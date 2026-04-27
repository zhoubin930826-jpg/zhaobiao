<template>
  <div>
    <div class="ivu-inline-block ivu-fr">
      <Dropdown @on-click="handleChangeTableSize" trigger="click">
        <Tooltip class="ivu-ml" content="密度" placement="top">
          <i-link>
            <Icon type="md-list" />
          </i-link>
        </Tooltip>
        <DropdownMenu slot="list">
          <DropdownItem name="default" :selected="tableSize === 'default'"
            >默认</DropdownItem
          >
          <DropdownItem name="large" :selected="tableSize === 'large'"
            >宽松</DropdownItem
          >
          <DropdownItem name="small" :selected="tableSize === 'small'"
            >紧凑</DropdownItem
          >
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
      <Dropdown trigger="click">
        <Tooltip class="ivu-ml" content="列设置" placement="top">
          <i-link>
            <Icon type="md-options" />
          </i-link>
        </Tooltip>
        <DropdownMenu slot="list">
          <div class="ivu-p-8">列展示</div>
          <Divider size="small" class="ivu-mt-8 ivu-mb-8" />
          <li
            class="ivu-dropdown-item"
            v-for="item in columns"
            :key="item.title"
            @click="item.show = !item.show"
          >
            <Checkbox v-model="item.show"></Checkbox>
            <span>{{ item.title }}</span>
          </li>
        </DropdownMenu>
      </Dropdown>
    </div>
    <div class="ivu-mt-16 ivu-mb-8">
      <Button type="primary" ghost icon="md-add" @click="handleOpenCreate">
        新增角色
      </Button>
    </div>
    <Table
      ref="table"
      :columns="tableColumns"
      :data="list"
      :loading="loading"
      :size="tableSize"
      class="ivu-mt"
    >
      <template slot-scope="{ row }" slot="builtIn">
        <Tag v-if="row.builtIn" color="blue">是</Tag>
        <Tag v-else>否</Tag>
      </template>
      <template slot-scope="{ row }" slot="menuCodes">
        <div v-if="Array.isArray(row.menuCodes) && row.menuCodes.length">
          <Tag
            v-for="code in row.menuCodes.slice(0, 3)"
            :key="code"
            color="blue"
          >
            {{ code }}
          </Tag>
          <Tag v-if="row.menuCodes.length > 3">
            +{{ row.menuCodes.length - 3 }}
          </Tag>
        </div>
        <span v-else>—</span>
      </template>
      <template slot-scope="{ row }" slot="action">
        <div @click.stop.prevent>
          <a type="text" @click="handleEdit(row)">编辑</a>
          <Divider type="vertical" />
          <a
            v-if="!row.builtIn"
            type="text"
            style="color: #ed4014"
            @click="handleDelete(row)"
            >删除</a
          >
          <span v-else style="color: #c5c8ce">删除</span>
        </div>
      </template>
    </Table>
    <div class="ivu-mt ivu-text-center">
      <Page
        :total="total"
        :current.sync="current"
        show-total
        show-sizer
        show-elevator
        :page-size="limit"
        @on-change="handleChangePage"
        @on-page-size-change="handleChangePageSize"
      />
    </div>
    <Modal
      v-model="modal.show"
      :title="modal.type === 'edit' ? '编辑角色' : '新增角色'"
      :transfer="false"
      width="560"
      @on-visible-change="onModalVisible"
    >
      <Form
        v-if="formReady"
        ref="roleForm"
        :model="roleForm"
        :rules="roleFormRules"
        label-position="top"
        label-colon
      >
        <FormItem prop="code" label="角色编码">
          <Input
            v-model="roleForm.code"
            placeholder="如 BID_MANAGER，英文字母与下划线"
            :disabled="modal.type === 'edit' && roleForm.builtIn"
          />
        </FormItem>
        <FormItem prop="name" label="角色名称">
          <Input v-model="roleForm.name" placeholder="请输入角色名称" />
        </FormItem>
        <FormItem prop="description" label="描述">
          <Input
            v-model="roleForm.description"
            type="textarea"
            :rows="2"
            placeholder="可选"
          />
        </FormItem>
        <FormItem prop="menuIds" label="菜单授权">
          <div class="role-menu-tree-wrap">
            <Tree
              v-if="menuList.length"
              :data="menuList"
              show-checkbox
              ref="menuTree"
              children-key="child"
              @on-check-change="syncMenuSelection"
            />
            <span v-else class="role-menu-tree-empty">暂无菜单数据</span>
          </div>
          <div style="font-size: 12px; margin-top: 6px; color: #808695">
            请勾选该角色可访问的菜单；后端会按已选菜单编码进行授权。
          </div>
        </FormItem>
      </Form>
      <template #footer>
        <Button @click="modal.show = false">取消</Button>
        <Button type="primary" :loading="submitting" @click="handleSubmit"
          >保存</Button
        >
      </template>
    </Modal>
  </div>
</template>

<script>
    import {
        listRoles,
        createRole,
        updateRole,
        deleteRole,
        listMenus
    } from '@api/system';

    function menuDtoNodesToTree (menus, checkedSet) {
        return (menus || []).map(m => ({
            id: m.id,
            title: m.name || m.code || String(m.id),
            expand: true,
            checked: checkedSet.has(Number(m.id)),
            child: menuDtoNodesToTree(m.children || [], checkedSet)
        }));
    }

    function normalizeIdArray (arr) {
        if (!Array.isArray(arr)) return [];
        return arr.map(id => Number(id)).filter(n => !Number.isNaN(n));
    }

    export default {
        data () {
            return {
                columns: [
                    {
                        title: 'ID',
                        key: 'id',
                        minWidth: 100,
                        show: false
                    },
                    {
                        title: '角色编码',
                        key: 'code',
                        minWidth: 140,
                        show: true
                    },
                    {
                        title: '角色名称',
                        key: 'name',
                        minWidth: 140,
                        show: true
                    },
                    {
                        title: '描述',
                        key: 'description',
                        minWidth: 200,
                        show: true
                    },
                    {
                        title: '内置',
                        key: 'builtIn',
                        slot: 'builtIn',
                        minWidth: 80,
                        show: true
                    },
                    {
                        title: '授权菜单',
                        key: 'menuCodes',
                        slot: 'menuCodes',
                        minWidth: 260,
                        show: true
                    },
                    {
                        title: '操作',
                        key: 'action',
                        slot: 'action',
                        minWidth: 200,
                        align: 'center',
                        fixed: 'right',
                        show: true
                    }
                ],
                loading: false,
                listAll: [],
                list: [],
                current: 1,
                limit: 10,
                total: 0,
                tableSize: 'default',
                tableFullscreen: false,
                modal: {
                    show: false,
                    type: 'new'
                },
                formReady: true,
                submitting: false,
                roleForm: {
                    id: null,
                    code: '',
                    name: '',
                    description: '',
                    builtIn: false,
                    menuIds: []
                },
                roleFormRules: {
                    code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
                    name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
                    menuIds: [
                        {
                            required: true,
                            type: 'array',
                            min: 1,
                            message: '请至少选择一个菜单',
                            trigger: 'change'
                        }
                    ]
                },
                menuRoots: [],
                /** 弹窗内菜单树勾选状态（菜单接口晚返回时用于补渲染） */
                modalMenuCheckedIds: [],
                menuList: []
            };
        },
        computed: {
            tableColumns () {
                return this.columns.filter(item => item.show);
            }
        },
        methods: {
            loadReference () {
                listMenus().then(res => {
                    this.menuRoots = Array.isArray(res) ? res : [];
                    if (this.modal.show) {
                        this.rebuildMenuTree(this.modalMenuCheckedIds);
                    }
                });
            },
            rebuildMenuTree (checkedIds) {
                const normalized = normalizeIdArray(checkedIds);
                this.roleForm.menuIds = normalized;
                const set = new Set(normalized);
                this.menuList = menuDtoNodesToTree(this.menuRoots, set);
            },
            getData () {
                if (this.loading) return;
                this.loading = true;
                listRoles().then(res => {
                    const rows = Array.isArray(res) ? res : [];
                    this.listAll = rows;
                    this.total = rows.length;
                    const start = (this.current - 1) * this.limit;
                    this.list = rows.slice(start, start + this.limit);
                }).finally(() => {
                    this.loading = false;
                });
            },
            handleChangeTableSize (size) {
                this.tableSize = size;
            },
            handleFullscreen () {
                this.tableFullscreen = !this.tableFullscreen;
                this.$emit('on-fullscreen', this.tableFullscreen);
            },
            handleRefresh () {
                this.getData();
            },
            handleChangePage (page) {
                this.current = page;
                this.applyPageSlice();
            },
            handleChangePageSize (size) {
                this.current = 1;
                this.limit = size;
                this.applyPageSlice();
            },
            applyPageSlice () {
                const rows = this.listAll;
                this.total = rows.length;
                const start = (this.current - 1) * this.limit;
                this.list = rows.slice(start, start + this.limit);
            },
            openFormModal () {
                this.formReady = false;
                this.$nextTick(() => {
                    this.formReady = true;
                    this.$nextTick(() => {
                        if (this.$refs.roleForm) {
                            this.$refs.roleForm.clearValidate();
                        }
                    });
                });
            },
            handleOpenCreate () {
                this.modal.type = 'new';
                this.modalMenuCheckedIds = [];
                this.roleForm = {
                    id: null,
                    code: '',
                    name: '',
                    description: '',
                    builtIn: false,
                    menuIds: []
                };
                this.rebuildMenuTree(this.modalMenuCheckedIds);
                this.modal.show = true;
                this.openFormModal();
            },
            handleEdit (row) {
                this.modal.type = 'edit';
                this.modalMenuCheckedIds = normalizeIdArray(row.menuIds);
                this.roleForm = {
                    id: row.id,
                    code: row.code || '',
                    name: row.name || '',
                    description: row.description || '',
                    builtIn: !!row.builtIn,
                    menuIds: normalizeIdArray(row.menuIds)
                };
                this.rebuildMenuTree(this.modalMenuCheckedIds);
                this.modal.show = true;
                this.openFormModal();
            },
            handleDelete (row) {
                if (row.builtIn) {
                    this.$Message.warning('内置角色不能删除');
                    return;
                }
                this.$Modal.confirm({
                    title: '确认删除',
                    content: `确定删除角色「${row.name}」吗？若已有用户使用该角色将无法删除。`,
                    onOk: () => {
                        return deleteRole(row.id)
                            .then(() => {
                                this.$Message.success('删除成功');
                                this.getData();
                            })
                            .catch(() => {});
                    }
                });
            },
            collectMenuIds () {
                const tree = this.$refs.menuTree;
                if (!tree || typeof tree.getCheckedNodes !== 'function') {
                    return [];
                }
                const nodes = tree.getCheckedNodes();
                return normalizeIdArray(nodes.map(n => n.id));
            },
            syncMenuSelection () {
                this.roleForm.menuIds = this.collectMenuIds();
                return this.roleForm.menuIds;
            },
            buildPayload () {
                return {
                    code: (this.roleForm.code || '').trim(),
                    name: (this.roleForm.name || '').trim(),
                    description: (this.roleForm.description || '').trim() || undefined,
                    menuIds: normalizeIdArray(this.roleForm.menuIds)
                };
            },
            handleSubmit () {
                this.syncMenuSelection();
                this.$refs.roleForm.validate(valid => {
                    if (!valid) return;
                    const payload = this.buildPayload();
                    if (!payload.menuIds.length) {
                        this.$Message.warning('请至少勾选一个菜单');
                        return;
                    }
                    this.submitting = true;
                    const done = () => {
                        this.submitting = false;
                    };
                    if (this.modal.type === 'new') {
                        createRole(payload)
                            .then(() => {
                                this.$Message.success('新增成功');
                                this.modal.show = false;
                                this.getData();
                            })
                            .finally(done);
                    } else {
                        updateRole(this.roleForm.id, payload)
                            .then(() => {
                                this.$Message.success('保存成功');
                                this.modal.show = false;
                                this.getData();
                            })
                            .finally(done);
                    }
                });
            },
            onModalVisible (visible) {
                if (!visible) {
                    this.menuList = [];
                    this.modalMenuCheckedIds = [];
                    this.roleForm.menuIds = [];
                }
            }
        },
        mounted () {
            this.loadReference();
        }
    };
</script>

<style lang="less" scoped>
.role-menu-tree-wrap {
    max-height: 280px;
    overflow: auto;
    padding: 8px 0;
    border: 1px solid #e8eaec;
    border-radius: 4px;
}
.role-menu-tree-empty {
    color: #808695;
    padding: 8px 12px;
    display: inline-block;
}
</style>
