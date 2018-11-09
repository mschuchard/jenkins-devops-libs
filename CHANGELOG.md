### Next
**Helm**
- Add chart provenance verification to applicable methods.
- Add `lint` method.
- `values` param is now an array of strings.

**Puppet**
- Fix `scope` param for `.task`.

### 1.1.0
**Helm**
- Added `context` param to applicable methods.
- Enabled multiple `set` parameter values for applicable methods.
- Changed `delete` usage to block DSL.

**Terraform**
- Changed `apply`, `destroy`, `plan`, and `validate` usage to block DSL.
- Added `var_file`, `var`, and `target` params to applicable methods.
- Added `plugin_install` method.

**Packer**
- Added `plugin_install` method.

**Puppet**
- Added `code_deploy` and `task` methods.

### 1.0.0
Initial release.
