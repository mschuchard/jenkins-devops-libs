### Next
**Helm**
- Add chart provenance verification to applicable methods.
- Add `lint` and `package` methods.
- `values` param is now an array of strings.

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
