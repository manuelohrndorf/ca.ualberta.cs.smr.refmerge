import os
import time
import threading
from flask import Flask, render_template_string
from flask_socketio import SocketIO

app = Flask(__name__)
socketio = SocketIO(app)

ROOT_DIRECTORY = '.'  # Default directory to monitor

def generate_file_tree(directory):
    """Recursively generate the file and folder structure as an HTML unordered list."""
    items = os.listdir(directory)
    html = "<ul>"
    for item in items:
        item_path = os.path.join(directory, item)
        if os.path.isdir(item_path):
            html += f'<li><strong>{item}/</strong>'
            html += generate_file_tree(item_path)  # Recursive call
            html += '</li>'
        else:
            html += f'<li>{item}'
            if item.endswith(".txt") or item.endswith(".java"):
                with open(item_path, 'r', encoding='utf-8', errors='ignore') as file:
                    content = file.read().replace("\n", "<br>").replace("\r", "")
                html += f'<div style="margin-left: 20px; border: 1px solid #ccc; padding: 10px; background: #f9f9f9;">{content}</div>'
            html += '</li>'
    html += "</ul>"
    return html

@app.route('/')
def index():
    """Serve the main page."""
    html_template = f"""
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Repository Report View</title>
        <style>
            body {{ font-family: Arial, sans-serif; margin: 20px; }}
            ul {{ list-style-type: none; padding-left: 20px; }}
            li {{ margin: 5px 0; }}
            li strong {{ color: #007BFF; }}
            div {{ font-family: monospace; white-space: pre-wrap; overflow-x: auto; }}
        </style>
        <script src="https://cdn.socket.io/4.0.0/socket.io.min.js"></script>
        <script>
            document.addEventListener("DOMContentLoaded", function() {{
                const socket = io();
                socket.on("update", function(data) {{
                    document.getElementById("file-tree").innerHTML = data;
                }});
            }});
        </script>
    </head>
    <body>
        <h1>Repository Report View</h1>
        <div id="file-tree">{generate_file_tree(ROOT_DIRECTORY)}</div>
    </body>
    </html>
    """
    return render_template_string(html_template)

def monitor_directory():
    """Monitor the directory for changes and send updates via WebSocket."""
    last_snapshot = None
    while True:
        current_snapshot = generate_file_tree(ROOT_DIRECTORY)
        if current_snapshot != last_snapshot:
            socketio.emit("update", current_snapshot)
            last_snapshot = current_snapshot
        time.sleep(1)  # Check for changes every second

if __name__ == "__main__":
    ROOT_DIRECTORY = './move_and_modify_method/move_and_modify_method.repository/src'

    #ROOT_DIRECTORY = './pull_up_and_move_method/pull_up_and_move_method.repository/src'

    #ROOT_DIRECTORY = './pull_up_and_move_method_conflict/pull_up_and_move_method_conflict.repository/src'

    threading.Thread(target=monitor_directory, daemon=True).start()
    socketio.run(app, host="127.0.0.1", port=5000)
