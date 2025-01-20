import os

def generate_file_tree(directory, base_path):
    """Recursively generate the file and folder structure as an HTML unordered list."""
    items = os.listdir(directory)
    html = "<ul>"
    for item in items:
        item_path = os.path.join(directory, item)
        relative_path = os.path.relpath(item_path, base_path)
        if os.path.isdir(item_path):
            html += f'<li><strong>{item}/</strong>'
            html += generate_file_tree(item_path, base_path)  # Recursive call
            html += '</li>'
        else:
            html += f'<li>{item}'
            if item.endswith(".java"):
                with open(item_path, 'r', encoding='utf-8', errors='ignore') as file:
                    content = file.read().replace("\n", "<br>").replace("\r", "")
                html += f'<div style="margin-left: 20px; border: 1px solid #ccc; padding: 10px; background: #f9f9f9;">{content}</div>'
            html += '</li>'
    html += "</ul>"
    return html

def generate_html_page(directory):
    """Generate the full HTML page with the file tree."""
    file_tree_html = generate_file_tree(directory, directory)
    html = f"""
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Repository Report</title>
        <style>
            body {{ font-family: Arial, sans-serif; margin: 20px; }}
            ul {{ list-style-type: none; padding-left: 20px; }}
            li {{ margin: 5px 0; }}
            li strong {{ color: #007BFF; }}
            div {{ font-family: monospace; white-space: pre-wrap; overflow-x: auto; }}
        </style>
    </head>
    <body>
        <h1>Repository Report</h1>
        {file_tree_html}
    </body>
    </html>
    """
    return html

def write_html_file(root_directory, output_path):
    """Write the generated HTML to a file."""
    html_content = generate_html_page(root_directory)
    with open(output_path, 'w', encoding='utf-8') as f:
        f.write(html_content)

if __name__ == "__main__":

    root_directory = './move_and_modify_method/move_and_modify_method.repository_plain'  # The root directory to report
    output_file = "./move_and_modify_method/report.html"  # The name of the output HTML file

    #root_directory = './pull_up_and_move_method/pull_up_and_move_method.repository_plain'  # The root directory to report
    #output_file = "./pull_up_and_move_method/report.html"  # The name of the output HTML file

    #root_directory = './pull_up_and_move_method_conflict/pull_up_and_move_method_conflict.repository_plain'  # The root directory to report
    #output_file = "./pull_up_and_move_method_conflict/report.html"  # The name of the output HTML file

    write_html_file(root_directory, output_file)
    print(f"HTML file generated: {output_file}")
