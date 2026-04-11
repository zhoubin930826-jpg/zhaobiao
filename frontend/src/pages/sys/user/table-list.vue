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
      <Button type="primary" @click="handleAdd" class="ivu-mr-8">
        <Icon type="md-add" /> 新增用户
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
      <template slot-scope="{ row }" slot="username">
        {{ row.username }}
      </template>
      <template slot-scope="{ row }" slot="status">
        <Badge v-if="row.status === 'APPROVED'" color="green" text="已通过" />
        <Badge v-else-if="row.status === 'PENDING'" color="gold" text="待审核" />
        <Badge v-else-if="row.status === 'REJECTED'" color="red" text="已驳回" />
        <Badge v-else-if="row.status === 'DISABLED'" color="default" text="已禁用" />
        <span v-else>{{ row.status }}</span>
      </template>
      <template slot-scope="{ row }" slot="roleNames">
        {{ (row.roleNames || []).join('、') || '—' }}
      </template>
      <template slot-scope="{ row }" slot="action">
        <div @click.stop.prevent>
          <a type="text" @click="handleView(row)">查看</a>
          <Divider type="vertical" />
          <a type="text" @click="handleEdit(row)">编辑</a>
          <Divider type="vertical" />
          <a type="text" @click="handleAudit(row)">审核</a>
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

    <!-- 用户新增/编辑/查看模态框 -->
    <Modal
      v-model="modal.show"
      :title="modalTitle"
      width="560"
      :before-close="handleCloseModal"
      :transfer="false"
    >
      <Form
        v-if="formReady"
        ref="userForm"
        :model="formData"
        :rules="formRules"
        :disabled="modal.type === 'view'"
        label-position="top"
      >
        <Row :gutter="32">
          <Col span="24">
            <FormItem label="用户名" prop="username">
              <Input
                v-model="formData.username"
                placeholder="请输入用户名"
                :disabled="modal.type === 'edit' || modal.type === 'view'"
              />
              <span v-if="modal.type === 'edit' || modal.type === 'view'" style="color: #999; font-size: 12px;">
                用户名不可修改
              </span>
            </FormItem>
          </Col>
        </Row>
        <Row :gutter="32" v-if="modal.type === 'add'">
          <Col span="24">
            <FormItem label="密码" prop="password">
              <Input
                v-model="formData.password"
                type="password"
                placeholder="请输入密码"
              />
            </FormItem>
          </Col>
        </Row>
        <Row :gutter="32">
          <Col span="24">
            <FormItem label="手机号" prop="phone">
              <Input
                v-model="formData.phone"
                placeholder="请输入手机号"
              />
            </FormItem>
          </Col>
        </Row>
        <Row :gutter="32" v-if="modal.type !== 'view'">
          <Col span="24">
            <FormItem label="角色" prop="roleIds">
              <Select
                v-model="formData.roleIds"
                multiple
                placeholder="请选择角色（可多选）"
              >
                <Option
                  :value="item.id"
                  v-for="item in roleList"
                  :key="item.id"
                >{{ item.name }}</Option>
              </Select>
            </FormItem>
          </Col>
        </Row>
        <Row :gutter="32" v-if="modal.type === 'view'">
          <Col span="24">
            <FormItem label="状态">
              <span>
                <Badge v-if="formData.status === 'APPROVED'" color="green" text="已通过" />
                <Badge v-else-if="formData.status === 'PENDING'" color="gold" text="待审核" />
                <Badge v-else-if="formData.status === 'REJECTED'" color="red" text="已驳回" />
                <Badge v-else-if="formData.status === 'DISABLED'" color="default" text="已禁用" />
                <span v-else>{{ formData.status }}</span>
              </span>
            </FormItem>
            <FormItem label="创建时间">
              <span>{{ formData.createdAt }}</span>
            </FormItem>
            <FormItem label="最后登录">
              <span>{{ formData.lastLoginAt || '—' }}</span>
            </FormItem>
            <FormItem label="当前角色">
              <span>{{ (formData.roleNames || []).join('、') || '—' }}</span>
            </FormItem>
          </Col>
        </Row>
      </Form>
      <template #footer>
        <Button v-if="modal.type === 'view'" @click="handleCloseModal">关闭</Button>
        <template v-else>
          <Button @click="handleCloseModal">取消</Button>
          <Button type="primary" @click="handleSubmit" :loading="submitting">保存</Button>
        </template>
      </template>
    </Modal>

    <!-- 审核模态框 -->
    <Modal
      v-model="auditModal.show"
      title="用户审核"
      width="460"
      :before-close="handleCloseAudit"
      :transfer="false"
    >
      <Form
        v-if="auditFormReady"
        ref="auditForm"
        :model="auditData"
        :rules="auditRules"
        label-position="top"
      >
        <Row :gutter="32">
          <Col span="24">
            <FormItem label="审核结果" prop="status">
              <RadioGroup v-model="auditData.status">
                <Radio label="APPROVED">通过</Radio>
                <Radio label="REJECTED">驳回</Radio>
              </RadioGroup>
            </FormItem>
          </Col>
        </Row>
        <Row :gutter="32" v-if="auditData.status === 'REJECTED'">
          <Col span="24">
            <FormItem label="驳回原因">
              <Input
                v-model="auditData.reason"
                type="textarea"
                :rows="3"
                placeholder="请输入驳回原因（选填）"
              />
            </FormItem>
          </Col>
        </Row>
      </Form>
      <template #footer>
        <Button @click="handleCloseAudit">取消</Button>
        <Button type="primary" @click="handleSubmitAudit" :loading="auditSubmitting">确定</Button>
      </template>
    </Modal>
  </div>
</template>

<script>
    import {
        listUsers,
        updateUserRoles,
        getRoleList,
        auditUser,
        DeleteUser
    } from '@api/system';
    import { AccountRegister } from '@api/account';
    import { cloneDeep } from 'lodash';

    export default {
        mounted () {
            getRoleList().then(res => {
                this.roleList = res;
            });
        },
        data () {
            return {
                userInfoRules: {
                    roleIds: [{ type: 'array', required: true, message: '请至少选择一个角色', trigger: 'change' }]
                },
                formRules: {
                    username: [
                        { required: true, message: '请输入用户名', trigger: 'blur' },
                        { min: 3, max: 20, message: '用户名长度应为3-20个字符', trigger: 'blur' }
                    ],
                    password: [
                        { required: true, message: '请输入密码', trigger: 'blur' },
                        { min: 6, message: '密码长度至少6个字符', trigger: 'blur' }
                    ],
                    phone: [
                        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
                    ]
                },
                auditRules: {
                    status: [{ required: true, message: '请选择审核结果', trigger: 'change' }]
                },
                userListAll: [],
                columns: [
                    {
                        title: '用户名',
                        key: 'username',
                        slot: 'username',
                        minWidth: 140,
                        show: true
                    },
                    {
                        title: '状态',
                        key: 'status',
                        slot: 'status',
                        minWidth: 100,
                        show: true
                    },
                    {
                        title: '手机号',
                        key: 'phone',
                        minWidth: 120,
                        show: true
                    },
                    {
                        title: '最后登录',
                        key: 'lastLoginAt',
                        minWidth: 160,
                        show: true
                    },
                    {
                        title: '创建时间',
                        key: 'createdAt',
                        minWidth: 180,
                        show: true
                    },
                    {
                        title: '用户 ID',
                        key: 'id',
                        minWidth: 120,
                        show: false
                    },
                    {
                        title: '角色',
                        key: 'roleNames',
                        minWidth: 160,
                        slot: 'roleNames',
                        show: true
                    },
                    {
                        title: '操作',
                        key: 'action',
                        slot: 'action',
                        minWidth: 220,
                        align: 'center',
                        fixed: 'right',
                        show: true
                    }
                ],
                roleList: [],
                loading: false,
                list: [],
                current: 1,
                limit: 10,
                total: 0,
                tableSize: 'default',
                tableFullscreen: false,
                // 用户表单
                formData: {
                    id: null,
                    username: '',
                    password: '',
                    phone: '',
                    roleIds: []
                },
                // 审核表单
                auditData: {
                    userId: null,
                    status: '',
                    reason: ''
                },
                formReady: true,
                auditFormReady: true,
                modal: {
                    show: false,
                    type: 'add' // add, edit, view, audit
                },
                auditModal: {
                    show: false
                },
                submitting: false,
                auditSubmitting: false
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
                    add: '新增用户',
                    edit: '编辑用户',
                    view: '查看用户'
                };
                return titles[this.modal.type] || '用户管理';
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
                        out = out.filter(u => (u.username || '').indexOf(q) > -1);
                    }
                    if (params.role_id !== '' && params.role_id != null) {
                        const rid = Number(params.role_id);
                        out = out.filter(u => (u.roleIds || []).some(id => Number(id) === rid));
                    }
                    return out;
                };
                listUsers().then(res => {
                    const rows = Array.isArray(res) ? res : [];
                    this.userListAll = rows;
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
            // 查找单一用户信息
            handleGetUser (id) {
                return this.list.find(item => item.id === id);
            },
            // 新增用户
            handleAdd () {
                this.formData = {
                    id: null,
                    username: '',
                    password: '',
                    phone: '',
                    roleIds: []
                };
                this.modal.type = 'add';
                this.modal.show = true;
                this.initForm();
            },
            // 编辑用户
            handleEdit (row) {
                this.formData = {
                    id: row.id,
                    username: row.username,
                    password: '',
                    phone: row.phone || '',
                    roleIds: Array.isArray(row.roleIds) ? [...row.roleIds] : []
                };
                this.modal.type = 'edit';
                this.modal.show = true;
                this.initForm();
            },
            // 查看用户
            handleView (row) {
                this.formData = cloneDeep(row);
                this.modal.type = 'view';
                this.modal.show = true;
                this.initForm();
            },
            // 删除用户
            handleDelete (row) {
                this.$Modal.confirm({
                    title: '确认删除',
                    content: `确定要删除用户「${row.username}」吗？删除后无法恢复！`,
                    onOk: () => {
                        DeleteUser(row.id).then(() => {
                            this.$Message.success('删除成功');
                            this.getData();
                        }).catch(err => {
                            // 如果后端接口不可用，显示提示
                            this.$Message.warning('删除用户功能暂不可用：' + (err.message || '接口未实现'));
                        });
                    }
                });
            },
            // 审核用户
            handleAudit (row) {
                this.auditData = {
                    userId: row.id,
                    status: row.status === 'PENDING' ? '' : (row.status === 'APPROVED' ? 'APPROVED' : 'REJECTED'),
                    reason: ''
                };
                this.auditModal.show = true;
                this.initAuditForm();
            },
            // 提交审核
            handleSubmitAudit () {
                this.$refs.auditForm.validate(valid => {
                    if (!valid) return;
                    this.auditSubmitting = true;
                    const auditRequest = {
                        approved: this.auditData.status === 'APPROVED',
                        reason: this.auditData.reason
                    };
                    auditUser(this.auditData.userId, auditRequest).then(() => {
                        this.auditSubmitting = false;
                        this.auditModal.show = false;
                        this.$Message.success('审核操作成功');
                        this.getData();
                    }).catch(() => {
                        this.auditSubmitting = false;
                    });
                });
            },
            // 关闭审核模态框
            handleCloseAudit () {
                this.auditModal.show = false;
            },
            // 关闭用户模态框
            handleCloseModal () {
                this.modal.show = false;
            },
            // 提交用户表单
            handleSubmit () {
                this.$refs.userForm.validate(valid => {
                    if (!valid) return;
                    this.submitting = true;
                    if (this.modal.type === 'add') {
                        // 新增用户 - 使用注册接口
                        const data = {
                            username: this.formData.username,
                            password: this.formData.password,
                            phone: this.formData.phone
                        };
                        AccountRegister(data).then(() => {
                            this.submitting = false;
                            this.modal.show = false;
                            this.$Message.success('新增用户成功');
                            this.getData();
                        }).catch(() => {
                            this.submitting = false;
                        });
                    } else if (this.modal.type === 'edit') {
                        // 编辑用户 - 更新角色
                        updateUserRoles(this.formData.id, {
                            roleIds: this.formData.roleIds || []
                        }).then(() => {
                            this.submitting = false;
                            this.modal.show = false;
                            this.$Message.success('用户信息已更新');
                            this.getData();
                        }).catch(() => {
                            this.submitting = false;
                        });
                    }
                });
            },
            initForm () {
                this.formReady = false;
                this.$nextTick(() => {
                    this.formReady = true;
                });
            },
            initAuditForm () {
                this.auditFormReady = false;
                this.$nextTick(() => {
                    this.auditFormReady = true;
                });
            }
        }
    };
</script>

<style lang="less" scoped>
.page-sys-user {
  &-modal {
    &-form {
      .ivu-input[disabled],
      fieldset[disabled] .ivu-input,
      .ivu-select-disabled .ivu-select-selection {
        background-color: #fff;
        color: inherit;
      }
      &-region {
        .i-region-transfer {
          width: 270px;
        }
        .i-region-drop-main {
          width: 270px !important;
          height: 200px;
          overflow: auto;
          column-count: 2;
          &-item {
            line-height: normal;
          }
        }
      }
    }
  }
}
</style>
