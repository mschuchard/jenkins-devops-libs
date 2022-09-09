resource "local_file" "file" {
  content  = "hello world"
  filename = "${path.root}/foo"
}
