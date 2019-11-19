### Next (Roadmap)
**General**
- Support new and old DSL.

**Helm**
- Enable native logging for `test` method if available.

**Terraform**
- Update `taint` method for new 0.12 usage.
- Add `return` parameter to `plan` method.
- Promote `fmt` method to supported.

### 1.3.1
**General**
- Add type specification checks.
- Fixed explicit List type check on params.

**Goss**
- Fix port default value in `server` method.

**Helm**
- Rename `package` method to `packages` to avoid reserved name collision.

**Packer**
- Fix bugs in methods.

**Terraform**
- Add `fmt` method as beta.
- Add `target` and `display` parameters to `plan` method.
- Rename `import` method to `imports` to avoid reserved name collision.
- Fix bugs in methods.

### 1.3.0
**Helm**
- Allow values override to also be a URL.
- Add `keyring` param to `package` method.
- Add `kubectl` method.

**Packer**
- Add `inspect` method.
- Change `only` parameter input type.

**Puppet**
- Add `token` method.

**Terraform**
- Add `taint`, `state`, and `import` methods.
- Add `check_vars` parameter to `validate` method.
- Add `destroy` parameter to `plan` method.
- Handle 0.12 changes to `validate` method.
- Checks for required parameters to methods.
- Change `dir` to `config_path` param for `destroy` method, and also allow for plan file arguments.

### 1.2.1
**General**
- Fix `map_to_json` common method and update per documented example.
- Various fixes, cleanup, and optimization.

**Goss**
- Fix flag setting and usage in `validate_docker`.

**OpenFaaS**
- Add necessary `function` and optional `tls` params to `invoke` method.

### 1.2.0
**Helm**
- Add chart provenance verification to applicable methods.
- Add `lint`, `package`, and `test` methods.
- `values` param is now an array of strings.

**OpenFaaS**
- Added `invoke`, `login`, `push`, and `remove` methods.
- Fixed `deploy` and `build` invalid usage issue.

**Puppet**
- Fix `scope` param for `.task`.

**Terraform**
- Changed `init` usage to block DSL and added `plugin_dir` and `upgrade` params.
- Modified `plugin_install` usage to block DSL and added `install_loc` param.
- `env.TF_IN_AUTOMATION` added to methods.

### 1.1.0
**Helm**
- Added `context` param to applicable methods.
- Enabled multiple `set` parameter values for applicable methods.
- Changed `delete` usage to block DSL.

**Packer**
- Added `plugin_install` method.

**Puppet**
- Added `code_deploy` and `task` methods.

**Terraform**
- Changed `apply`, `destroy`, `plan`, and `validate` usage to block DSL.
- Added `var_file`, `var`, and `target` params to applicable methods.
- Added `plugin_install` method.

### 1.0.0
Initial release.
