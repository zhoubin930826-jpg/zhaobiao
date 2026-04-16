<template>
  <div>
    <div class="i-layout-page-header">
      <PageHeader title="会员管理" hidden-breadcrumb />
    </div>
    <Card :bordered="false" dis-hover class="ivu-mt" ref="card">
      <div class="ivu-inline-block ivu-fr">
        <Dropdown @on-click="handleChangeTableSize" trigger="click">
          <Tooltip class="ivu-ml" content="密度" placement="top">
            <i-link><Icon type="md-list" /></i-link>
          </Tooltip>
          <DropdownMenu slot="list">
            <DropdownItem name="default" :selected="tableSize === 'default'">默认</DropdownItem>
            <DropdownItem name="large" :selected="tableSize === 'large'">宽松</DropdownItem>
            <DropdownItem name="small" :selected="tableSize === 'small'">紧凑</DropdownItem>
          </DropdownMenu>
        </Dropdown>
        <Tooltip class="ivu-ml" :content="tableFullscreen ? '退出全屏' : '全屏'" placement="top">
          <i-link @click.native="handleFullscreen">
            <Icon :custom="tableFullscreen ? 'i-icon i-icon-exit-full-screen' : 'i-icon i-icon-full-screen'" />
          </i-link>
        </Tooltip>
        <Tooltip class="ivu-ml" content="刷新" placement="top">
          <i-link @click.native="getData"><Icon custom="i-icon i-icon-refresh" /></i-link>
        </Tooltip>
        <Dropdown trigger="click">
          <Tooltip class="ivu-ml" content="列设置" placement="top">
            <i-link><Icon type="md-options" /></i-link>
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
          <Icon type="md-add" /> 新增会员
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
        <template slot="status" slot-scope="{ row }">
          <Badge v-if="row.status === 'ENABLED'" color="green" text="启用" />
          <Badge v-else-if="row.status === 'DISABLED'" color="default" text="禁用" />
          <span v-else>{{ row.status }}</span>
        </template>
        <template slot="download" slot-scope="{ row }">
          <Badge v-if="row.canDownloadFile" color="green" text="允许" />
          <Badge v-else color="default" text="不允许" />
        </template>
        <template slot="businessTypes" slot-scope="{ row }">
          {{ formatBusinessTypes(row.businessTypes) }}
        </template>
        <template slot="action" slot-scope="{ row }">
          <div @click.stop.prevent>
            <a @click="handleEdit(row)">编辑</a>
            <Divider type="vertical" />
            <a @click="toggleStatus(row)">{{ row.status === 'ENABLED' ? '禁用' : '启用' }}</a>
            <Divider type="vertical" />
            <a @click="toggleDownload(row)">{{ row.canDownloadFile ? '关闭下载' : '开启下载' }}</a>
            <Divider type="vertical" />
            <a style="color: #ed4014" @click="resetPassword(row)">重置密码</a>
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
        :title="modal.type === 'add' ? '新增会员' : '编辑会员'"
        width="620"
        :before-close="handleCloseModal"
        :transfer="false"
      >
        <Form ref="memberForm" :model="formData" :rules="formRules" label-position="top">
          <Row :gutter="16">
            <Col span="12">
              <FormItem label="用户名" prop="username">
                <Input v-model="formData.username" :disabled="modal.type === 'edit'" placeholder="请输入用户名" />
              </FormItem>
            </Col>
            <Col span="12">
              <FormItem label="手机号" prop="phone">
                <Input v-model="formData.phone" placeholder="请输入手机号" />
              </FormItem>
            </Col>
          </Row>
          <Row :gutter="16">
            <Col span="12">
              <FormItem label="邮箱" prop="email">
                <Input v-model="formData.email" placeholder="请输入邮箱" />
              </FormItem>
            </Col>
            <Col span="12">
              <FormItem label="真实姓名">
                <Input v-model="formData.realName" placeholder="请输入真实姓名（选填）" />
              </FormItem>
            </Col>
          </Row>
          <Row :gutter="16">
            <Col span="12">
              <FormItem label="公司名称" prop="companyName">
                <Input v-model="formData.companyName" placeholder="请输入公司名称" />
              </FormItem>
            </Col>
            <Col span="12">
              <FormItem label="联系人" prop="contactPerson">
                <Input v-model="formData.contactPerson" placeholder="请输入联系人" />
              </FormItem>
            </Col>
          </Row>
          <Row :gutter="16">
            <Col span="24">
              <FormItem label="统一社会信用代码" prop="unifiedSocialCreditCode">
                <Input v-model="formData.unifiedSocialCreditCode" placeholder="请输入18位统一社会信用代码" />
              </FormItem>
            </Col>
          </Row>
          <Row :gutter="16">
            <Col span="12">
              <FormItem label="密码" prop="password">
                <Input v-model="formData.password" type="password" :placeholder="modal.type === 'add' ? '请输入密码（6-32位）' : '编辑时无需填写密码'" />
              </FormItem>
            </Col>
            <Col span="12">
              <FormItem label="确认密码" prop="confirmPassword">
                <Input v-model="formData.confirmPassword" type="password" :placeholder="modal.type === 'add' ? '请再次输入密码' : '编辑时无需填写确认密码'" />
              </FormItem>
            </Col>
          </Row>
          <Row :gutter="16">
            <Col span="24">
              <FormItem label="会员类型" prop="businessTypeIds">
                <Select v-model="formData.businessTypeIds" multiple placeholder="请选择会员类型">
                  <Option v-for="item in businessTypeOptions" :key="item.id" :value="item.id">{{ item.name }}</Option>
                </Select>
              </FormItem>
            </Col>
          </Row>
          <Row :gutter="16">
            <Col span="12">
              <FormItem label="会员状态" prop="status">
                <RadioGroup v-model="formData.status">
                  <Radio label="ENABLED">启用</Radio>
                  <Radio label="DISABLED">禁用</Radio>
                </RadioGroup>
              </FormItem>
            </Col>
            <Col span="12">
              <FormItem label="下载权限">
                <i-switch v-model="formData.canDownloadFile" />
              </FormItem>
            </Col>
          </Row>
        </Form>
        <template #footer>
          <Button @click="handleCloseModal">取消</Button>
          <Button type="primary" :loading="submitting" @click="handleSubmit">保存</Button>
        </template>
      </Modal>
    </Card>
  </div>
</template>

<script>
    import screenfull from 'screenfull';
    import {
        listMembers,
        createMember,
        updateMember,
        getMemberDetail,
        listBusinessTypeOptions,
        updateMemberStatus,
        updateMemberDownloadAccess,
        resetMemberPassword
    } from '@api/system';

    export default {
        name: 'system-member',
        data () {
            return {
                loading: false,
                list: [],
                allList: [],
                submitting: false,
                current: 1,
                limit: 10,
                total: 0,
                tableSize: 'default',
                tableFullscreen: false,
                resetPasswordValue: '',
                businessTypeOptions: [],
                modal: {
                    show: false,
                    type: 'add'
                },
                formData: {
                    id: null,
                    username: '',
                    phone: '',
                    email: '',
                    companyName: '',
                    contactPerson: '',
                    unifiedSocialCreditCode: '',
                    realName: '',
                    password: '',
                    confirmPassword: '',
                    businessTypeIds: [],
                    canDownloadFile: false,
                    status: 'ENABLED'
                },
                formRules: {
                    username: [
                        { required: true, message: '请输入用户名', trigger: 'blur' },
                        { min: 4, max: 64, message: '用户名长度需在4-64位之间', trigger: 'blur' }
                    ],
                    phone: [
                        { required: true, message: '请输入手机号', trigger: 'blur' },
                        { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
                    ],
                    email: [
                        { required: true, message: '请输入邮箱', trigger: 'blur' },
                        { type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }
                    ],
                    companyName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
                    contactPerson: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
                    unifiedSocialCreditCode: [
                        { required: true, message: '请输入统一社会信用代码', trigger: 'blur' },
                        { pattern: /^[0-9A-Z]{18}$/, message: '统一社会信用代码格式不正确', trigger: 'blur' }
                    ],
                    password: [
                        {
                            validator: (rule, value, callback) => {
                                if (this.modal.type === 'edit') {
                                    callback();
                                    return;
                                }
                                if (!value) {
                                    callback(new Error('请输入密码'));
                                    return;
                                }
                                callback();
                            },
                            trigger: 'blur'
                        },
                        { min: 6, max: 32, message: '密码长度需在6-32位之间', trigger: 'blur' }
                    ],
                    confirmPassword: [
                        {
                            validator: (rule, value, callback) => {
                                if (this.modal.type === 'edit') {
                                    callback();
                                    return;
                                }
                                if (!value) {
                                    callback(new Error('请输入确认密码'));
                                    return;
                                }
                                if (value !== this.formData.password) {
                                    callback(new Error('两次输入密码不一致'));
                                    return;
                                }
                                callback();
                            },
                            trigger: 'blur'
                        }
                    ],
                    businessTypeIds: [{ type: 'array', required: true, min: 1, message: '请至少选择一个会员类型', trigger: 'change' }]
                },
                columns: [
                    { title: '用户名', key: 'username', minWidth: 120, show: true },
                    { title: '公司名称', key: 'companyName', minWidth: 180, show: true },
                    { title: '联系人', key: 'contactPerson', minWidth: 120, show: true },
                    { title: '手机号', key: 'phone', minWidth: 130, show: true },
                    { title: '邮箱', key: 'email', minWidth: 180, show: true },
                    { title: '业务类型', slot: 'businessTypes', minWidth: 180, show: true },
                    { title: '下载权限', slot: 'download', minWidth: 100, show: true },
                    { title: '状态', slot: 'status', minWidth: 100, show: true },
                    { title: '操作', slot: 'action', minWidth: 280, align: 'center', fixed: 'right', show: true }
                ]
            };
        },
        computed: {
            tableColumns () {
                return this.columns.filter(item => item.show);
            }
        },
        mounted () {
            this.loadBusinessTypeOptions();
            this.getData();
        },
        methods: {
            loadBusinessTypeOptions () {
                listBusinessTypeOptions().then(res => {
                    this.businessTypeOptions = Array.isArray(res) ? res : [];
                });
            },
            getData () {
                if (this.loading) return;
                this.loading = true;
                listMembers().then(res => {
                    this.allList = Array.isArray(res) ? res : [];
                    this.total = this.allList.length;
                    this.refreshPageData();
                    this.loading = false;
                }).catch(() => {
                    this.loading = false;
                });
            },
            refreshPageData () {
                const start = (this.current - 1) * this.limit;
                this.list = this.allList.slice(start, start + this.limit);
            },
            handleChangePage (page) {
                this.current = page;
                this.refreshPageData();
            },
            handleChangePageSize (size) {
                this.current = 1;
                this.limit = size;
                this.refreshPageData();
            },
            handleChangeTableSize (size) {
                this.tableSize = size;
            },
            handleFullscreen () {
                this.tableFullscreen = !this.tableFullscreen;
                if (this.tableFullscreen) {
                    screenfull.request(this.$refs.card.$el);
                } else {
                    screenfull.exit();
                }
            },
            handleAdd () {
                this.formData = {
                    id: null,
                    username: '',
                    phone: '',
                    email: '',
                    companyName: '',
                    contactPerson: '',
                    unifiedSocialCreditCode: '',
                    realName: '',
                    password: '',
                    confirmPassword: '',
                    businessTypeIds: [],
                    canDownloadFile: false,
                    status: 'ENABLED'
                };
                this.modal = {
                    show: true,
                    type: 'add'
                };
                this.$nextTick(() => {
                    if (this.$refs.memberForm) {
                        this.$refs.memberForm.resetFields();
                    }
                });
            },
            handleEdit (row) {
                getMemberDetail(row.id).then(res => {
                    this.formData = {
                        id: res.id,
                        username: res.username || '',
                        phone: res.phone || '',
                        email: res.email || '',
                        companyName: res.companyName || '',
                        contactPerson: res.contactPerson || '',
                        unifiedSocialCreditCode: res.unifiedSocialCreditCode || '',
                        realName: res.realName || '',
                        password: '',
                        confirmPassword: '',
                        businessTypeIds: Array.isArray(res.businessTypes) ? res.businessTypes.map(item => item.id) : [],
                        canDownloadFile: !!res.canDownloadFile,
                        status: res.status || 'ENABLED'
                    };
                    this.modal = {
                        show: true,
                        type: 'edit'
                    };
                    this.$nextTick(() => {
                        if (this.$refs.memberForm) {
                            this.$refs.memberForm.clearValidate();
                        }
                    });
                });
            },
            handleCloseModal () {
                this.modal.show = false;
            },
            handleSubmit () {
                this.$refs.memberForm.validate(valid => {
                    if (!valid) return;
                    this.submitting = true;
                    const request = this.modal.type === 'add'
                        ? createMember({
                            username: this.formData.username,
                            phone: this.formData.phone,
                            email: this.formData.email,
                            companyName: this.formData.companyName,
                            contactPerson: this.formData.contactPerson,
                            unifiedSocialCreditCode: this.formData.unifiedSocialCreditCode,
                            realName: this.formData.realName,
                            password: this.formData.password,
                            confirmPassword: this.formData.confirmPassword,
                            businessTypeIds: this.formData.businessTypeIds,
                            canDownloadFile: this.formData.canDownloadFile,
                            status: this.formData.status
                        })
                        : updateMember(this.formData.id, {
                            phone: this.formData.phone,
                            email: this.formData.email,
                            companyName: this.formData.companyName,
                            contactPerson: this.formData.contactPerson,
                            unifiedSocialCreditCode: this.formData.unifiedSocialCreditCode,
                            realName: this.formData.realName,
                            businessTypeIds: this.formData.businessTypeIds
                    });
                    request.then(() => {
                        this.submitting = false;
                        this.modal.show = false;
                        this.$Message.success(this.modal.type === 'add' ? '新增会员成功' : '编辑会员成功');
                        this.current = 1;
                        this.getData();
                    }).catch(() => {
                        this.submitting = false;
                    });
                });
            },
            formatBusinessTypes (items) {
                if (!Array.isArray(items) || !items.length) return '—';
                return items.map(item => item.name).join('、');
            },
            toggleStatus (row) {
                const status = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED';
                updateMemberStatus(row.id, { status }).then(() => {
                    this.$Message.success('状态更新成功');
                    this.getData();
                });
            },
            toggleDownload (row) {
                const canDownloadFile = !row.canDownloadFile;
                updateMemberDownloadAccess(row.id, { canDownloadFile }).then(() => {
                    this.$Message.success('下载权限更新成功');
                    this.getData();
                });
            },
            resetPassword (row) {
                this.resetPasswordValue = '';
                this.$Modal.confirm({
                    title: `重置 ${row.username} 的密码`,
                    render: h => h('Input', {
                        props: {
                            value: this.resetPasswordValue,
                            type: 'password',
                            placeholder: '请输入新密码（至少6位）'
                        },
                        on: {
                            input: val => { this.resetPasswordValue = val; }
                        }
                    }),
                    onOk: () => {
                        const password = (this.resetPasswordValue || '').trim();
                        if (password.length < 6) {
                            this.$Message.error('密码长度至少6位');
                            return Promise.reject(new Error('invalid password'));
                        }
                        return resetMemberPassword(row.id, { password, confirmPassword: password }).then(() => {
                            this.$Message.success('重置密码成功');
                        });
                    }
                });
            }
        }
    };
</script>
