resource "local_file" "file" {
  content = "hello world"

  # prefix inventory with name of first instance for now
  filename        = "${path.root}/foo"
  file_permission = "0644"
}
