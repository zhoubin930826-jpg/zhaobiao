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
      <Button type="primary" @click="handleAdd">
        <Icon type="md-add" /> 新增权限
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
      <template slot-scope="{ row }" slot="code">
        <Tag color="blue">{{ row.code }}</Tag>
      </template>
      <template slot-scope="{ row }" slot="name">
        <strong>{{ row.name }}</strong>
      </template>
      <template slot-scope="{ row }" slot="description">
        {{ row.description || '—' }}
      </template>
      <template slot-scope="{ row }" slot="action">
        <div @click.stop.prevent>
          <a type="text" @click="handleEdit(row)">编辑</a>
          <Divider type="vertical" />
          <a type="text" @click="handleView(row)">查看</a>
          <Divider type="vertical" />
          <a type="text" style="color: #ed4014" @click="handleDelete(row)">删除</a>
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

    <!-- 新增/编辑/查看模态框 -->
    <Modal
      v-model="modal.show"
      :title="modalTitle"
      width="560"
      :loading="modalLoading"
      :before-close="handleCloseModal"
      :transfer="false"
    >
      <Form
        v-if="formReady"
        ref="permissionForm"
        :model="formData"
        :rules="formRules"
        :disabled="modal.type === 'view'"
        label-position="top"
      >
        <Row :gutter="32">
          <Col span="24">
            <FormItem label="权限编码" prop="code">
              <Input
                v-model="formData.code"
                placeholder="请输入权限编码，如：dashboard:view"
                :disabled="modal.type === 'edit'"
              />
              <span v-if="modal.type === 'edit'" style="color: #999; font-size: 12px;">
                权限编码不可修改
              </span>
            </FormItem>
          </Col>
        </Row>
        <Row :gutter="32">
          <Col span="24">
            <FormItem label="权限名称" prop="name">
              <Input
                v-model="formData.name"
                placeholder="请输入权限名称，如：查看工作台"
              />
            </FormItem>
          </Col>
        </Row>
        <Row :gutter="32">
          <Col span="24">
            <FormItem label="权限描述" prop="description">
              <Input
                v-model="formData.description"
                type="textarea"
                :rows="3"
                placeholder="请输入权限描述"
              />
            </FormItem>
          </Col>
        </Row>
        <Row :gutter="32" v-if="modal.type === 'view'">
          <Col span="24">
            <FormItem label="权限 ID">
              <span>{{ formData.id }}</span>
            </FormItem>
          </Col>
        </Row>
      </Form>
      <template #footer>
        <Button v-if="modal.type === 'view'" @click="modal.show = false">关闭</Button>
        <template v-else>
          <Button @click="handleCloseModal">取消</Button>
          <Button type="primary" @click="handleSubmit" :loading="submitting">保存</Button>
        </template>
      </template>
    </Modal>
  </div>
</template>

<script>
    import {
        listPermissions,
        createPermission,
        updatePermission,
        deletePermission
    } from '@api/system';
    import { cloneDeep } from 'lodash';

    export default {
        data () {
            return {
                columns: [
                    {
                        title: 'ID',
                        key: 'id',
                        minWidth: 80,
                        show: false
                    },
                    {
                        title: '权限编码',
                        key: 'code',
                        slot: 'code',
                        minWidth: 200,
                        show: true
                    },
                    {
                        title: '权限名称',
                        key: 'name',
                        slot: 'name',
                        minWidth: 160,
                        show: true
                    },
                    {
                        title: '描述',
                        key: 'description',
                        slot: 'description',
                        minWidth: 200,
                        show: true
                    },
                    {
                        title: '操作',
                        key: 'action',
                        slot: 'action',
                        minWidth: 180,
                        align: 'center',
                        fixed: 'right',
                        show: true
                    }
                ],
                loading: false,
                list: [],
                allList: [],
                current: 1,
                limit: 10,
                total: 0,
                tableSize: 'default',
                tableFullscreen: false,
                // 模态框相关
                modal: {
                    show: false,
                    type: 'add' // add, edit, view
                },
                modalLoading: false,
                formReady: true,
                formData: {
                    id: null,
                    code: '',
                    name: '',
                    description: ''
                },
                originalFormData: '',
                submitting: false,
                formRules: {
                    code: [
                        { required: true, message: '请输入权限编码', trigger: 'blur' },
                        { pattern: /^[a-z]+(:[a-z]+)*$/, message: '权限编码格式应为小写字母，使用冒号分隔，如：dashboard:view', trigger: 'blur' }
                    ],
                    name: [
                        { required: true, message: '请输入权限名称', trigger: 'blur' },
                        { max: 50, message: '权限名称不能超过50个字符', trigger: 'blur' }
                    ],
                    description: [
                        { max: 200, message: '描述不能超过200个字符', trigger: 'blur' }
                    ]
                }
            };
        },
        computed: {
            // 动态设置列
            tableColumns () {
                const columns = [...this.columns];
                return columns.filter(item => item.show);
            },
            offset () {
                return (this.current - 1) * this.limit;
            },
            modalTitle () {
                const titles = {
                    add: '新增权限',
                    edit: '编辑权限',
                    view: '查看权限'
                };
                return titles[this.modal.type] || '权限管理';
            },
            modalLoading () {
                return false;
            }
        },
        methods: {
            getData () {
                if (this.loading) return;
                this.loading = true;
                const params = this.$parent.$parent.$refs.form.data;
                const applyFilter = (rows) => {
                    let out = rows || [];
                    if (params.name) {
                        const q = String(params.name).trim();
                        out = out.filter(p => (p.name || '').includes(q));
                    }
                    if (params.code) {
                        const q = String(params.code).trim().toLowerCase();
                        out = out.filter(p => (p.code || '').toLowerCase().includes(q));
                    }
                    return out;
                };
                listPermissions().then(res => {
                    const rows = Array.isArray(res) ? res : (res.data || []);
                    this.allList = rows;
                    const filtered = applyFilter(rows);
                    this.total = filtered.length;
                    const start = (this.current - 1) * this.limit;
                    this.list = filtered.slice(start, start + this.limit);
                    this.loading = false;
                }).catch(() => {
                    this.loading = false;
                });
            },
            // 改变表格尺寸
            handleChangeTableSize (size) {
                this.tableSize = size;
            },
            // 表格全屏
            handleFullscreen () {
                this.tableFullscreen = !this.tableFullscreen;
                this.$emit('on-fullscreen', this.tableFullscreen);
            },
            // 刷新表格数据
            handleRefresh () {
                this.getData();
            },
            // 切换页码
            handleChangePage (page) {
                this.current = page;
                this.getData();
            },
            // 切换每页条数
            handleChangePageSize (size) {
                this.current = 1;
                this.limit = size;
                this.getData();
            },
            // 新增权限
            handleAdd () {
                this.formData = {
                    id: null,
                    code: '',
                    name: '',
                    description: ''
                };
                this.modal.type = 'add';
                this.modal.show = true;
                this.initForm();
            },
            // 编辑权限
            handleEdit (row) {
                this.formData = cloneDeep(row);
                this.originalFormData = JSON.stringify(this.formData);
                this.modal.type = 'edit';
                this.modal.show = true;
                this.initForm();
            },
            // 查看权限
            handleView (row) {
                this.formData = cloneDeep(row);
                this.modal.type = 'view';
                this.modal.show = true;
                this.initForm();
            },
            // 删除权限
            handleDelete (row) {
                this.$Modal.confirm({
                    title: '确认删除',
                    content: `确定要删除权限「${row.name}」(${row.code})吗？删除后无法恢复！`,
                    onOk: () => {
                        deletePermission(row.id).then(() => {
                            this.$Message.success('删除成功');
                            this.getData();
                        }).catch(() => {
                            this.$Message.error('删除失败');
                        });
                    }
                });
            },
            // 关闭模态框
            handleCloseModal () {
                if (this.modal.type === 'add' || this.modal.type === 'edit') {
                    const currentData = JSON.stringify(this.formData);
                    if (currentData !== this.originalFormData && this.originalFormData) {
                        this.$Modal.confirm({
                            title: '关闭确认',
                            content: '您已经修改了权限信息，是否直接关闭？',
                            onOk: () => {
                                this.modal.show = false;
                            }
                        });
                    } else {
                        this.modal.show = false;
                    }
                } else {
                    this.modal.show = false;
                }
            },
            // 提交表单
            handleSubmit () {
                this.$refs.permissionForm.validate(valid => {
                    if (!valid) return;
                    this.submitting = true;
                    const data = {
                        code: this.formData.code,
                        name: this.formData.name,
                        description: this.formData.description
                    };
                    if (this.modal.type === 'add') {
                        createPermission(data).then(() => {
                            this.submitting = false;
                            this.modal.show = false;
                            this.$Message.success('新增成功');
                            this.getData();
                        }).catch(() => {
                            this.submitting = false;
                        });
                    } else if (this.modal.type === 'edit') {
                        updatePermission(this.formData.id, data).then(() => {
                            this.submitting = false;
                            this.modal.show = false;
                            this.$Message.success('更新成功');
                            this.getData();
                        }).catch(() => {
                            this.submitting = false;
                        });
                    }
                });
            },
            // 初始化表单
            initForm () {
                this.formReady = false;
                this.$nextTick(() => {
                    this.formReady = true;
                    if (this.modal.type !== 'view') {
                        this.$nextTick(() => {
                            this.$refs.permissionForm.resetFields();
                            this.originalFormData = JSON.stringify(this.formData);
                        });
                    }
                });
            }
        }
    };
</script>

<style lang="less" scoped>
.page-sys-permission {
  &-modal {
    &-form {
      .ivu-input[disabled],
      fieldset[disabled] .ivu-input,
      .ivu-select-disabled .ivu-select-selection {
        background-color: #fff;
        color: inherit;
      }
    }
  }
}
</style>
