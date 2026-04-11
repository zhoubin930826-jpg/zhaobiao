<template>
  <div>
    <Row :gutter="16" class="ivu-mb-8">
      <Col :xs="24" :sm="12" :md="8" :lg="6">
        <Input
          v-model="keyword"
          clearable
          placeholder="按名称或编码筛选"
          @on-enter="handleSearch"
        />
      </Col>
      <Col>
        <Button type="primary" @click="handleSearch">查询</Button>
        <Button class="ivu-ml-8" @click="handleResetSearch">重置</Button>
      </Col>
    </Row>
    <div class="ivu-inline-block ivu-fr">
      <Dropdown @on-click="handleChangeTableSize" trigger="click">
        <Tooltip class="ivu-ml" content="密度" placement="top">
          <i-link>
            <Icon type="md-list" />
          </i-link>
        </Tooltip>
        <DropdownMenu slot="list">
          <DropdownItem name="default" :selected="tableSize === 'default'">默认</DropdownItem>
          <DropdownItem name="large" :selected="tableSize === 'large'">宽松</DropdownItem>
          <DropdownItem name="small" :selected="tableSize === 'small'">紧凑</DropdownItem>
        </DropdownMenu>
      </Dropdown>
      <Tooltip
        class="ivu-ml"
        :content="tableFullscreen ? '退出全屏' : '全屏'"
        placement="top"
      >
        <i-link @click.native="handleFullscreen">
          <Icon
            :custom="
              tableFullscreen
                ? 'i-icon i-icon-exit-full-screen'
                : 'i-icon i-icon-full-screen'
            "
          />
        </i-link>
      </Tooltip>
      <Tooltip class="ivu-ml" content="刷新" placement="top">
        <i-link @click.native="handleRefresh">
          <Icon custom="i-icon i-icon-refresh" />
        </i-link>
      </Tooltip>
    </div>
    <div class="ivu-mt-16 ivu-mb-8">
      <Button type="primary" @click="handleAddRoot">
        <Icon type="md-add" /> 新增顶级菜单
      </Button>
    </div>
    <Table
      row-key="id"
      :columns="columns"
      :data="displayTree"
      :loading="loading"
      :size="tableSize"
      class="ivu-mt page-sys-menu-table"
    >
      <template slot-scope="{ row }" slot="name">
        <strong>{{ row.name }}</strong>
      </template>
      <template slot-scope="{ row }" slot="type">
        <Tag>{{ menuTypeLabel(row.type) }}</Tag>
      </template>
      <template slot-scope="{ row }" slot="routePath">
        {{ row.routePath || '—' }}
      </template>
      <template slot-scope="{ row }" slot="flags">
        <Tag v-if="row.visible" color="blue">显示</Tag>
        <Tag v-else>隐藏</Tag>
        <Tag v-if="row.enabled" color="green" class="ivu-ml-4">启用</Tag>
        <Tag v-else color="default" class="ivu-ml-4">停用</Tag>
      </template>
      <template slot-scope="{ row }" slot="action">
        <div @click.stop.prevent>
          <a type="text" @click="handleAddChild(row)">新增子菜单</a>
          <Divider type="vertical" />
          <a type="text" @click="handleEdit(row)">编辑</a>
          <Divider type="vertical" />
          <a type="text" style="color: #ed4014" @click="handleDelete(row)">删除</a>
        </div>
      </template>
    </Table>

    <Modal
      v-model="modal.show"
      :title="modalTitle"
      width="640"
      :transfer="false"
      @on-visible-change="onModalVisible"
    >
      <Form
        v-if="formReady"
        ref="menuForm"
        :model="form"
        :rules="formRules"
        label-position="top"
      >
        <Row :gutter="16">
          <Col span="12">
            <FormItem label="菜单编码" prop="code">
              <Input
                v-model="form.code"
                placeholder="如 SYSTEM_USER"
                :disabled="modal.mode === 'edit'"
              />
            </FormItem>
          </Col>
          <Col span="12">
            <FormItem label="菜单名称" prop="name">
              <Input v-model="form.name" placeholder="显示名称" />
            </FormItem>
          </Col>
          <Col span="12">
            <FormItem label="菜单类型" prop="type">
              <Select v-model="form.type" placeholder="请选择">
                <Option value="DIRECTORY">目录</Option>
                <Option value="MENU">菜单</Option>
                <Option value="BUTTON">按钮</Option>
              </Select>
            </FormItem>
          </Col>
          <Col span="12">
            <FormItem label="上级菜单" prop="parentId">
              <Select
                v-model="form.parentId"
                clearable
                filterable
                placeholder="清空表示顶级菜单"
              >
                <Option
                  v-for="opt in parentSelectOptions"
                  :key="opt.id"
                  :value="opt.id"
                  :label="opt.label"
                >
                  {{ opt.label }}
                </Option>
              </Select>
            </FormItem>
          </Col>
          <Col span="12">
            <FormItem label="路由路径" prop="routePath">
              <Input v-model="form.routePath" placeholder="如 /system/user" />
            </FormItem>
          </Col>
          <Col span="12">
            <FormItem label="前端组件" prop="component">
              <Input v-model="form.component" placeholder="如 sys/user/index" />
            </FormItem>
          </Col>
          <Col span="12">
            <FormItem label="图标" prop="icon">
              <Input v-model="form.icon" placeholder="iView 图标名，可选" />
            </FormItem>
          </Col>
          <Col span="12">
            <FormItem label="排序" prop="sortOrder">
              <InputNumber
                v-model="form.sortOrder"
                :min="0"
                :step="1"
                style="width: 100%"
              />
            </FormItem>
          </Col>
          <Col span="12">
            <FormItem label="是否显示" prop="visible">
              <i-switch v-model="form.visible" />
            </FormItem>
          </Col>
          <Col span="12">
            <FormItem label="是否启用" prop="enabled">
              <i-switch v-model="form.enabled" />
            </FormItem>
          </Col>
          <Col span="12">
            <FormItem label="权限编码" prop="permissionCode">
              <Input v-model="form.permissionCode" placeholder="可选，如 menu:view" />
            </FormItem>
          </Col>
          <Col span="24">
            <FormItem label="描述" prop="description">
              <Input
                v-model="form.description"
                type="textarea"
                :rows="2"
                placeholder="可选"
              />
            </FormItem>
          </Col>
        </Row>
      </Form>
      <template #footer>
        <Button @click="modal.show = false">取消</Button>
        <Button type="primary" :loading="submitting" @click="handleSubmit">保存</Button>
      </template>
    </Modal>
  </div>
</template>

<script>
    import {
        listMenus,
        createMenu,
        updateAdminMenu,
        deleteMenu
    } from '@api/system';
    import { menuDtosToSysTree } from '@/libs/bid-menu';
    import { cloneDeep } from 'lodash';

    function childToChildren (nodes) {
        if (!nodes || !nodes.length) return [];
        return nodes.map(n => {
            const { child, ...rest } = n;
            return {
                ...rest,
                children: childToChildren(child)
            };
        });
    }

    function filterMenuTree (nodes, keyword) {
        if (!keyword) return nodes;
        const q = keyword.trim().toLowerCase();
        const walk = (list) => {
            const out = [];
            (list || []).forEach(n => {
                const children = walk(n.children);
                const match = (n.name && n.name.toLowerCase().includes(q)) || (n.code && String(n.code).toLowerCase().includes(q));
                if (match || children.length) {
                    out.push({ ...n, children });
                }
            });
            return out;
        };
        return walk(nodes);
    }

    function collectSubtreeIds (row, acc = new Set()) {
        if (!row) return acc;
        acc.add(row.id);
        (row.children || []).forEach(c => collectSubtreeIds(c, acc));
        return acc;
    }

    function findNodeById (nodes, id) {
        for (const n of nodes || []) {
            if (n.id === id) return n;
            const found = findNodeById(n.children, id);
            if (found) return found;
        }
        return null;
    }

    function flattenForParent (nodes, depth = 0, out = []) {
        (nodes || []).forEach(n => {
            const pad = '\u3000'.repeat(depth);
            out.push({ id: n.id, label: `${pad}${n.name}` });
            if (n.children && n.children.length) {
                flattenForParent(n.children, depth + 1, out);
            }
        });
        return out;
    }

    const defaultForm = () => ({
        id: null,
        code: '',
        name: '',
        type: 'MENU',
        parentId: null,
        routePath: '',
        component: '',
        icon: '',
        sortOrder: 0,
        visible: true,
        enabled: true,
        permissionCode: '',
        description: ''
    });

    export default {
        data () {
            return {
                keyword: '',
                keywordApplied: '',
                loading: false,
                tableSize: 'default',
                tableFullscreen: false,
                treeData: [],
                modal: {
                    show: false,
                    mode: 'add'
                },
                formReady: true,
                form: defaultForm(),
                editingRow: null,
                defaultParentId: null,
                submitting: false,
                formRules: {
                    code: [{ required: true, message: '请输入菜单编码', trigger: 'blur' }],
                    name: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
                    type: [{ required: true, message: '请选择类型', trigger: 'change' }],
                    sortOrder: [
                        {
                            required: true,
                            type: 'number',
                            message: '请填写排序',
                            trigger: 'change'
                        }
                    ]
                },
                columns: [
                    {
                        title: '名称',
                        slot: 'name',
                        key: 'name',
                        tree: true,
                        minWidth: 220,
                        ellipsis: true
                    },
                    { title: '编码', key: 'code', minWidth: 140, ellipsis: true },
                    { title: '类型', slot: 'type', minWidth: 100, align: 'center' },
                    { title: '路由', slot: 'routePath', minWidth: 160, ellipsis: true },
                    { title: '组件', key: 'component', minWidth: 140, ellipsis: true },
                    { title: '排序', key: 'sortOrder', width: 72, align: 'center' },
                    { title: '状态', slot: 'flags', minWidth: 160 },
                    {
                        title: '操作',
                        slot: 'action',
                        width: 220,
                        align: 'center',
                        fixed: 'right'
                    }
                ]
            };
        },
        computed: {
            displayTree () {
                return filterMenuTree(this.treeData, this.keywordApplied);
            },
            modalTitle () {
                if (this.modal.mode === 'edit') return '编辑菜单';
                if (this.defaultParentId != null && this.defaultParentId !== '') {
                    return '新增子菜单';
                }
                return '新增菜单';
            },
            parentSelectOptions () {
                const exclude = new Set();
                if (this.modal.mode === 'edit' && this.editingRow) {
                    collectSubtreeIds(this.editingRow, exclude);
                }
                const flat = flattenForParent(this.treeData);
                return flat.filter(o => !exclude.has(o.id));
            }
        },
        mounted () {
            this.loadTree();
        },
        methods: {
            menuTypeLabel (t) {
                const map = { DIRECTORY: '目录', MENU: '菜单', BUTTON: '按钮' };
                return map[t] || t || '—';
            },
            loadTree () {
                this.loading = true;
                listMenus().then(res => {
                    const rows = Array.isArray(res) ? res : [];
                    const sys = menuDtosToSysTree(rows);
                    this.treeData = childToChildren(sys);
                }).finally(() => {
                    this.loading = false;
                });
            },
            handleSearch () {
                this.keywordApplied = this.keyword;
            },
            handleResetSearch () {
                this.keyword = '';
                this.keywordApplied = '';
            },
            handleChangeTableSize (size) {
                this.tableSize = size;
            },
            handleFullscreen () {
                this.tableFullscreen = !this.tableFullscreen;
                this.$emit('on-fullscreen', this.tableFullscreen);
            },
            handleRefresh () {
                this.loadTree();
            },
            handleAddRoot () {
                this.modal.mode = 'add';
                this.editingRow = null;
                this.defaultParentId = null;
                this.openFormModal(defaultForm());
            },
            handleAddChild (row) {
                this.modal.mode = 'add';
                this.editingRow = null;
                this.defaultParentId = row.id;
                const f = defaultForm();
                f.parentId = row.id;
                this.openFormModal(f);
            },
            handleEdit (row) {
                this.modal.mode = 'edit';
                const full = findNodeById(this.treeData, row.id);
                this.editingRow = full ? cloneDeep(full) : cloneDeep(row);
                this.defaultParentId = null;
                const f = defaultForm();
                f.id = row.id;
                f.code = row.code || '';
                f.name = row.name || '';
                f.type = row.type || 'MENU';
                f.parentId = row.parentId != null ? row.parentId : null;
                f.routePath = row.routePath || '';
                f.component = row.component || '';
                f.icon = row.icon || '';
                f.sortOrder =
                    row.sortOrder != null ? Number(row.sortOrder) : 0;
                f.visible = row.visible !== false;
                f.enabled = row.enabled !== false;
                f.permissionCode = row.permissionCode || '';
                f.description = row.description || '';
                this.openFormModal(f);
            },
            openFormModal (form) {
                this.form = cloneDeep(form);
                this.modal.show = true;
                this.formReady = false;
                this.$nextTick(() => {
                    this.formReady = true;
                    this.$nextTick(() => {
                        if (this.$refs.menuForm) {
                            this.$refs.menuForm.clearValidate();
                        }
                    });
                });
            },
            onModalVisible (visible) {
                if (!visible) {
                    this.editingRow = null;
                    this.defaultParentId = null;
                }
            },
            handleDelete (row) {
                const hasChildren = row.children && row.children.length;
                if (hasChildren) {
                    this.$Message.warning('请先删除子菜单');
                    return;
                }
                this.$Modal.confirm({
                    title: '确认删除',
                    content: `确定删除「${row.name}」吗？`,
                    onOk: () => {
                        return deleteMenu(row.id)
                            .then(() => {
                                this.$Message.success('删除成功');
                                this.loadTree();
                            })
                            .catch(() => {});
                    }
                });
            },
            handleSubmit () {
                this.$refs.menuForm.validate(valid => {
                    if (!valid) return;
                    this.submitting = true;
                    const payload = {
                        code: this.form.code,
                        name: this.form.name,
                        type: this.form.type,
                        parentId:
                            this.form.parentId === undefined ||
                            this.form.parentId === '' ||
                            this.form.parentId === null
                                ? null
                                : Number(this.form.parentId),
                        routePath: this.form.routePath || '',
                        component: this.form.component || '',
                        icon: this.form.icon || '',
                        sortOrder: this.form.sortOrder,
                        visible: this.form.visible,
                        enabled: this.form.enabled,
                        permissionCode: this.form.permissionCode || '',
                        description: this.form.description || ''
                    };
                    const done = () => {
                        this.submitting = false;
                        this.modal.show = false;
                        this.$Message.success('保存成功');
                        this.loadTree();
                    };
                    const fail = () => {
                        this.submitting = false;
                    };
                    if (this.modal.mode === 'edit') {
                        updateAdminMenu(this.form.id, payload)
                            .then(done)
                            .catch(fail);
                    } else {
                        createMenu(payload).then(done).catch(fail);
                    }
                });
            }
        }
    };
</script>

<style lang="less" scoped>
.ivu-ml-4 {
  margin-left: 4px;
}

/* 树形列：缩进、展开按钮与名称同一行，不换行 */
.page-sys-menu-table ::v-deep td:first-child .ivu-table-cell {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
}

.page-sys-menu-table ::v-deep td:first-child .ivu-table-cell-tree-level,
.page-sys-menu-table ::v-deep td:first-child .ivu-table-cell-tree {
  flex-shrink: 0;
}

.page-sys-menu-table ::v-deep td:first-child .ivu-table-cell-slot {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}
</style>
