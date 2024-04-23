#!/bin/bash

# Kill the game-server process
pkill -f /opt/game-server/game-server/bin/game-server > /dev/null 2>&1

# Always exit successfully
exit 0