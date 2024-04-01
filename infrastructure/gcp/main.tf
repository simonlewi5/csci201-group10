locals {
  ssh_keys = [for file in var.public_key_paths : format("%s:%s", 
              trimspace(substr(basename(file), 0, length(basename(file)) - length(".pub"))), 
              trimspace(file("${path.module}/${file}"))
             )]
  ssh_keys_metadata = join("\n", local.ssh_keys)
}

resource "google_compute_network" "vpc_network" {
  name = "vpc-network"
  auto_create_subnetworks = true
}

resource "google_compute_global_address" "private_ip_range" {
    name = "private-ip-range"
    purpose = "VPC_PEERING"
    address_type = "INTERNAL"
    prefix_length = 24
    network = google_compute_network.vpc_network.self_link
}

resource "google_service_networking_connection" "private_vpc_connection" {
    network = google_compute_network.vpc_network.self_link
    service = "servicenetworking.googleapis.com"
    reserved_peering_ranges = [google_compute_global_address.private_ip_range.name]
}

resource "google_compute_instance" "game_server" {
    name         = "game-server"
    machine_type = "e2-micro"
    zone         = "${var.region}-a"
    allow_stopping_for_update = true

    boot_disk {
        initialize_params {
            image = "ubuntu-os-cloud/ubuntu-2204-lts"
        }
    }

    network_interface {
        network = google_compute_network.vpc_network.self_link

        access_config {
            // Ephemeral IP
        }
    }

    service_account {
        scopes = ["cloud-platform"]
    }

    metadata = {
        ssh-keys = local.ssh_keys_metadata
    }

    tags = ["http-server", "https-server"]
}

resource "google_compute_firewall" "http" {
    name = "http"
    network = google_compute_network.vpc_network.name


    allow {
        protocol = "tcp"
        ports = ["80", "443"]
    }

    source_ranges = ["0.0.0.0/0"]

    target_tags = ["http-server"]
}

resource "google_compute_firewall" "ssh" {
    name = "ssh"
    network = google_compute_network.vpc_network.name

    allow {
        protocol = "tcp"
        ports = ["22"]
    }

    source_ranges = ["0.0.0.0/0"]
    target_tags = ["game-server"]
}

resource "google_compute_firewall" "icmp" {
    name = "icmp"
    network = "default"

    allow {
        protocol = "icmp"
    }

    source_ranges = ["0.0.0.0/0"]
    target_tags = ["game-server"]
}

// Storage bucket

resource "google_storage_bucket" "game_assets_bucket" {
    name = "game-assets-bucket-${var.project_id}"
    location = var.region
    storage_class = "STANDARD"   
}

// Cloud SQL MySQL instance

resource "google_sql_database_instance" "game_db" {
    name = "game-db"
    database_version = "MYSQL_8_0"
    region = var.region
    settings {
        tier = "db-f1-micro"
        disk_size = 10
        disk_type = "PD_SSD"
        ip_configuration {
            ipv4_enabled = false
            private_network = google_compute_network.vpc_network.self_link
        }
        backup_configuration {
            enabled = true
        }
        location_preference {
            zone = "${var.region}-a"
        }
    }
}

resource "google_sql_user" "default" {
    name = "admin"
    instance = google_sql_database_instance.game_db.name
    password = var.db_password
}

