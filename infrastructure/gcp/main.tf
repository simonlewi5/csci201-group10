locals {
  ssh_keys = [for file in var.public_key_paths : trimspace(file("${path.module}/${file}"))]
  ssh_keys_metadata = join("\n", local.ssh_keys)
}

resource "google_compute_instance" "game_server" {
    name         = "game-server"
    machine_type = "e2-micro"
    zone         = "${var.region}-a"

    boot_disk {
        initialize_params {
            image = "ubuntu-os-cloud/ubuntu-2204-lts"
        }
    }

    network_interface {
        network = "default"

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
    network = "default"

    allow {
        protocol = "tcp"
        ports = ["80", "443"]
    }

    source_ranges = ["0.0.0.0/0"]

    target_tags = ["http-server"]
}

resource "google_compute_firewall" "ssh" {
    name = "ssh"
    network = "default"

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
            ipv4_enabled = true
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

