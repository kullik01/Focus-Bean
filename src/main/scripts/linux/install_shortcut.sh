#!/bin/bash

# Focus Bean Desktop Shortcut Installer

# Determine the directory where this script is located (resolved to absolute path)
SCRIPT_DIR="$(dirname "$(realpath "$0")")"
ICON_PATH="$SCRIPT_DIR/logo.png"
EXEC_PATH="$SCRIPT_DIR/bin/FocusBean"
MENU_DESKTOP_FILE="$HOME/.local/share/applications/FocusBean.desktop"

# Detect the user's desktop directory (respects localized folder names)
DESKTOP_DIR="$(xdg-user-dir DESKTOP 2>/dev/null || echo "$HOME/Desktop")"
DESKTOP_DESKTOP_FILE="$DESKTOP_DIR/FocusBean.desktop"

# Check if icon exists
if [ ! -f "$ICON_PATH" ]; then
    echo "Error: logo.png not found in $SCRIPT_DIR"
    exit 1
fi

# Check if executable exists
if [ ! -f "$EXEC_PATH" ]; then
    echo "Error: bin/FocusBean not found in $SCRIPT_DIR"
    exit 1
fi

echo "Installing Focus Bean shortcuts..."

# Build the .desktop file content once
DESKTOP_CONTENT="[Desktop Entry]
Name=Focus Bean
Comment=A modern timer application for deep work and productivity
Exec=$EXEC_PATH
Icon=$ICON_PATH
Terminal=false
Type=Application
Categories=Utility;Application;"

# --- Application menu shortcut ---
mkdir -p "$HOME/.local/share/applications"
echo "$DESKTOP_CONTENT" > "$MENU_DESKTOP_FILE"
chmod +x "$MENU_DESKTOP_FILE"

# Update desktop database to refresh the menu immediately
update-desktop-database "$HOME/.local/share/applications" 2>/dev/null

echo "  Added to application menu."

# --- Desktop icon ---
if [ -d "$DESKTOP_DIR" ]; then
    echo "$DESKTOP_CONTENT" > "$DESKTOP_DESKTOP_FILE"
    chmod +x "$DESKTOP_DESKTOP_FILE"

    # Mark as trusted so the desktop environment shows the icon without prompting
    gio set "$DESKTOP_DESKTOP_FILE" "metadata::trusted" true 2>/dev/null

    echo "  Added desktop icon to $DESKTOP_DIR."
else
    echo "  Desktop directory ($DESKTOP_DIR) not found, skipping desktop icon."
fi

echo ""
echo "Success! Focus Bean has been added to your application menu and desktop."
echo "You can now search for 'Focus Bean' in your launcher or double-click the desktop icon."
