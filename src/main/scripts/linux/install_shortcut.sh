#!/bin/bash

# Focus Bean Desktop Shortcut Installer

# Determine the directory where this script is located (resolved to absolute path)
SCRIPT_DIR="$(dirname "$(realpath "$0")")"
ICON_PATH="$SCRIPT_DIR/logo.png"
EXEC_PATH="$SCRIPT_DIR/bin/FocusBean"
DESKTOP_FILE="$HOME/.local/share/applications/FocusBean.desktop"

# Check if icon exists
if [ ! -f "$ICON_PATH" ]; then
    echo "Error: icon.png not found in $SCRIPT_DIR"
    exit 1
fi

# Check if executable exists
if [ ! -f "$EXEC_PATH" ]; then
    echo "Error: bin/FocusBean not found in $SCRIPT_DIR"
    exit 1
fi

echo "Installing Focus Bean desktop shortcut..."

# Create the .desktop file content
cat > "$DESKTOP_FILE" << EOF
[Desktop Entry]
Name=Focus Bean
Comment=A modern timer application for deep work and productivity
Exec=$EXEC_PATH
Icon=$ICON_PATH
Terminal=false
Type=Application
Categories=Utility;Application;
EOF

# Make the desktop file executable (optional but good practice)
chmod +x "$DESKTOP_FILE"

# Update desktop database to refresh the menu immediately
update-desktop-database "$HOME/.local/share/applications" 2>/dev/null

echo "Success! Focus Bean has been added to your application menu."
echo "You can now search for 'Focus Bean' in your launcher."
