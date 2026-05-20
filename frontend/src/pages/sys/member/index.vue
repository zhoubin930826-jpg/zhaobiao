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
          <i-link @click.native="getData"
            ><Icon custom="i-icon i-icon-refresh"
          /></i-link>
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
          <Badge
            v-else-if="row.status === 'DISABLED'"
            color="default"
            text="未启用"
          />
          <span v-else>{{ row.status }}</span>
        </template>
        <template slot="expiresAt" slot-scope="{ row }">
          <div>
            <Badge
              v-if="row.expired"
              color="red"
              text="已过期"
              style="margin-right: 8px"
            />
            <span>{{ formatDisplayDate(row.expiresAt) }}</span>
          </div>
        </template>
        <template slot="download" slot-scope="{ row }">
          <Badge v-if="row.canDownloadFile" color="green" text="允许" />
          <Badge v-else color="default" text="不允许" />
        </template>
        <template slot="businessTypes" slot-scope="{ row }">
          {{ formatBusinessTypes(row.businessTypes) }}
        </template>
        <template slot="firstLoginAt" slot-scope="{ row }">
          <span>{{ formatDisplayDate(row.firstLoginAt) }}</span>
        </template>
        <template slot="lastLoginAt" slot-scope="{ row }">
          <span>{{ formatDisplayDate(row.lastLoginAt) }}</span>
        </template>
        <template slot="businessLicense" slot-scope="{ row }">
          <div v-if="row.businessLicenseFileId">
            <a
              @click.prevent="
                previewProfileFile(
                  normalizeProfileFileFromResponse(row, 'business'),
                  '营业执照'
                )
              "
              >{{
                row.businessLicenseFileName ||
                `文件#${row.businessLicenseFileId}`
              }}</a
            >
            <div class="table-file-meta">
              <span>{{ formatFileSize(row.businessLicenseFileSize) }}</span>
              <span v-if="row.businessLicenseContentType" class="ivu-ml-4">{{
                row.businessLicenseContentType
              }}</span>
            </div>
          </div>
          <span v-else>未上传</span>
        </template>
        <template slot="performanceFile" slot-scope="{ row }">
          <div v-if="row.threeYearPerformanceFileId">
            <a
              @click.prevent="
                previewProfileFile(
                  normalizeProfileFileFromResponse(row, 'performance'),
                  '近三年业绩证明'
                )
              "
              >{{
                row.threeYearPerformanceFileName ||
                `文件#${row.threeYearPerformanceFileId}`
              }}</a
            >
            <div class="table-file-meta">
              <span>{{ formatFileSize(row.threeYearPerformanceFileSize) }}</span>
              <span
                v-if="row.threeYearPerformanceContentType"
                class="ivu-ml-4"
                >{{ row.threeYearPerformanceContentType }}</span
              >
            </div>
          </div>
          <span v-else>未上传</span>
        </template>
        <template slot="action" slot-scope="{ row }">
          <div @click.stop.prevent>
            <a @click="handleEdit(row)">编辑</a>
            <Divider type="vertical" />
            <a @click="toggleStatus(row)">{{
              row.status === "ENABLED" ? "禁用" : "启用"
            }}</a>
            <Divider type="vertical" />
            <a @click="toggleDownload(row)">{{
              row.canDownloadFile ? "关闭下载" : "开启下载"
            }}</a>
            <Divider type="vertical" />
            <a style="color: #ed4014" @click="resetPassword(row)">重置密码</a>
            <Divider type="vertical" />
            <a style="color: #ed4014" @click="removeMember(row)">删除</a>
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
        <Form
          ref="memberForm"
          :model="formData"
          :rules="formRules"
          label-position="top"
        >
          <Row :gutter="16">
            <Col span="12">
              <FormItem label="用户名" prop="username">
                <Input
                  v-model="formData.username"
                  :disabled="modal.type === 'edit'"
                  placeholder="请输入用户名"
                />
              </FormItem>
            </Col>
            <Col span="12">
              <FormItem label="联系方式" prop="phone">
                <Input v-model="formData.phone" placeholder="请输入联系方式" />
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
                <Input
                  v-model="formData.realName"
                  placeholder="请输入真实姓名（选填）"
                />
              </FormItem>
            </Col>
          </Row>
          <Row :gutter="16">
            <Col span="12">
              <FormItem label="公司名称" prop="companyName">
                <Input
                  v-model="formData.companyName"
                  placeholder="请输入公司名称"
                />
              </FormItem>
            </Col>
            <Col span="12">
              <FormItem label="联系人" prop="contactPerson">
                <Input
                  v-model="formData.contactPerson"
                  placeholder="请输入联系人"
                />
              </FormItem>
            </Col>
          </Row>
          <Row :gutter="16">
            <Col span="12">
              <FormItem label="营业执照上传">
                <Upload
                  type="drag"
                  :before-upload="handleBeforeUploadBusinessLicense"
                  :show-upload-list="false"
                >
                  <div style="padding: 12px 0">
                    <Icon
                      type="ios-cloud-upload"
                      size="20"
                      style="color: #2d8cf0"
                    ></Icon>
                    <p>拖拽或点击上传营业执照（单个文件）</p>
                  </div>
                </Upload>
              <div class="ivu-mt-8" v-if="businessLicense">
                <div class="attachment-item">
                  <div class="attachment-text">
                    <div class="attachment-name">
                      {{
                        businessLicense.fileName ||
                        `文件#${businessLicense.fileId}`
                      }}
                    </div>
                    <div class="attachment-meta">
                      <span>{{ formatFileSize(businessLicense.fileSize) }}</span>
                      <span
                        v-if="businessLicense.contentType"
                        class="ivu-ml-8"
                        >{{ businessLicense.contentType }}</span
                      >
                      <span
                        v-if="businessLicense.thumbnailStatus"
                        class="ivu-ml-8"
                      >
                        {{ formatThumbnailStatus(businessLicense.thumbnailStatus) }}
                      </span>
                    </div>
                  </div>
                  <div class="attachment-actions">
                    <a
                      class="ivu-mr-8"
                      @click.prevent="
                        previewProfileFile(businessLicense, '营业执照')
                      "
                      >查看</a
                    >
                    <Divider type="vertical" />
                    <a @click.prevent="removeBusinessLicense">移除</a>
                  </div>
                </div>
              </div>
              <div class="ivu-mt-8" v-else>
                <span class="ivu-text-muted">未上传营业执照</span>
              </div>
              </FormItem>
            </Col>
            <Col span="12">
              <FormItem label="近三年业绩证明">
                <Upload
                  type="drag"
                  :before-upload="handleBeforeUploadPerformance"
                  :show-upload-list="false"
                >
                  <div style="padding: 12px 0">
                    <Icon
                      type="ios-cloud-upload"
                      size="20"
                      style="color: #2d8cf0"
                    ></Icon>
                    <p>点击或拖拽上传近三年业绩证明（单个文件）</p>
                  </div>
                </Upload>
              <div class="ivu-mt-8" v-if="performanceFile">
                <div class="attachment-item">
                  <div class="attachment-text">
                    <div class="attachment-name">
                      {{
                        performanceFile.fileName ||
                        `文件#${performanceFile.fileId}`
                      }}
                    </div>
                    <div class="attachment-meta">
                      <span>{{ formatFileSize(performanceFile.fileSize) }}</span>
                      <span
                        v-if="performanceFile.contentType"
                        class="ivu-ml-8"
                        >{{ performanceFile.contentType }}</span
                      >
                      <span
                        v-if="performanceFile.thumbnailStatus"
                        class="ivu-ml-8"
                      >
                        {{ formatThumbnailStatus(performanceFile.thumbnailStatus) }}
                      </span>
                    </div>
                  </div>
                  <div class="attachment-actions">
                    <a
                      class="ivu-mr-8"
                      @click.prevent="
                        previewProfileFile(performanceFile, '近三年业绩证明')
                      "
                      >查看</a
                    >
                    <Divider type="vertical" />
                    <a @click.prevent="removePerformanceFile">移除</a>
                  </div>
                </div>
              </div>
              <div class="ivu-mt-8" v-else>
                <span class="ivu-text-muted">未上传业绩证明</span>
              </div>
              </FormItem>
            </Col>
          </Row>
          <Row :gutter="16">
            <Col span="24">
              <FormItem label="统一社会信用代码" prop="unifiedSocialCreditCode">
                <Input
                  v-model="formData.unifiedSocialCreditCode"
                  placeholder="请输入18位统一社会信用代码"
                />
              </FormItem>
            </Col>
          </Row>
          <Row :gutter="16">
            <Col span="12">
              <FormItem label="密码" prop="password">
                <Input
                  v-model="formData.password"
                  type="password"
                  :placeholder="
                    modal.type === 'add'
                      ? '请输入密码（6-32位）'
                      : '编辑时无需填写密码'
                  "
                />
              </FormItem>
            </Col>
            <Col span="12">
              <FormItem label="确认密码" prop="confirmPassword">
                <Input
                  v-model="formData.confirmPassword"
                  type="password"
                  :placeholder="
                    modal.type === 'add'
                      ? '请再次输入密码'
                      : '编辑时无需填写确认密码'
                  "
                />
              </FormItem>
            </Col>
          </Row>
          <Row :gutter="16">
            <Col span="24">
              <FormItem label="会员类型" prop="businessTypeIds">
                <Select
                  v-model="formData.businessTypeIds"
                  multiple
                  placeholder="请选择会员类型"
                >
                  <Option
                    v-for="item in businessTypeOptions"
                    :key="item.id"
                    :value="item.id"
                    >{{ item.name }}</Option
                  >
                </Select>
              </FormItem>
            </Col>
          </Row>
          <Row :gutter="16">
            <Col span="24">
              <FormItem label="会员过期时间" prop="expiresAt">
                <DatePicker
                  v-model="formData.expiresAt"
                  type="datetime"
                  format="yyyy-MM-dd HH:mm:ss"
                  placeholder="请选择会员过期时间"
                  style="width: 100%"
                />
              </FormItem>
            </Col>
          </Row>
          <Row :gutter="16">
            <Col span="12">
              <FormItem label="会员状态" prop="status">
                <RadioGroup v-model="formData.status" @on-change="onMemberStatusChange">
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
        <template slot="footer">
          <Button @click="handleCloseModal">取消</Button>
          <Button type="primary" :loading="submitting" @click="handleSubmit"
            >保存</Button
          >
        </template>
      </Modal>
    </Card>
  </div>
</template>

<script>
import screenfull from "screenfull";
import Setting from "@/setting";
import {
  listMembers,
  createMember,
  updateMember,
  getMemberDetail,
  listBusinessTypeOptions,
  updateMemberStatus,
  updateMemberDownloadAccess,
  resetMemberPassword,
  deleteMember,
  uploadMemberProfileFiles,
} from "@api/system";

export default {
  name: "system-member",
  data() {
    return {
      loading: false,
      list: [],
      allList: [],
      submitting: false,
      current: 1,
      limit: 10,
      total: 0,
      tableSize: "default",
      tableFullscreen: false,
      resetPasswordValue: "",
      businessTypeOptions: [],
      modal: {
        show: false,
        type: "add",
      },
      formData: {
        id: null,
        username: "",
        phone: "",
        email: "",
        companyName: "",
        contactPerson: "",
        unifiedSocialCreditCode: "",
        realName: "",
        password: "",
        confirmPassword: "",
        businessTypeIds: [],
        expiresAt: null,
        canDownloadFile: false,
        status: "ENABLED",
        businessLicenseFileId: null,
        threeYearPerformanceFileId: null,
      },
      // local files for preview/remove
      performanceFile: null,
      businessLicense: null,
      formRules: {
        username: [
          { required: true, message: "请输入用户名", trigger: "blur" },
          {
            min: 4,
            max: 64,
            message: "用户名长度需在4-64位之间",
            trigger: "blur",
          },
        ],
        phone: [{ required: true, message: "请输入联系方式", trigger: "blur" }],
        email: [
          { required: true, message: "请输入邮箱", trigger: "blur" },
          { type: "email", message: "请输入正确的邮箱", trigger: "blur" },
        ],
        companyName: [
          { required: true, message: "请输入公司名称", trigger: "blur" },
        ],
        contactPerson: [
          { required: true, message: "请输入联系人", trigger: "blur" },
        ],
        unifiedSocialCreditCode: [
          {
            required: true,
            message: "请输入统一社会信用代码",
            trigger: "blur",
          },
          {
            pattern: /^[0-9A-Z]{18}$/,
            message: "统一社会信用代码格式不正确",
            trigger: "blur",
          },
        ],
        password: [
          {
            validator: (rule, value, callback) => {
              if (this.modal.type === "edit") {
                callback();
                return;
              }
              if (!value) {
                callback(new Error("请输入密码"));
                return;
              }
              callback();
            },
            trigger: "blur",
          },
          {
            min: 6,
            max: 32,
            message: "密码长度需在6-32位之间",
            trigger: "blur",
          },
        ],
        confirmPassword: [
          {
            validator: (rule, value, callback) => {
              if (this.modal.type === "edit") {
                callback();
                return;
              }
              if (!value) {
                callback(new Error("请输入确认密码"));
                return;
              }
              if (value !== this.formData.password) {
                callback(new Error("两次输入密码不一致"));
                return;
              }
              callback();
            },
            trigger: "blur",
          },
        ],
        businessTypeIds: [
          {
            validator: (rule, value, callback) => {
              if (this.modal.type === "add") {
                if (!Array.isArray(value) || value.length < 1) {
                  callback(new Error("请至少选择一个会员类型"));
                  return;
                }
                callback();
                return;
              }
              if (this.formData.status !== "ENABLED") {
                callback();
                return;
              }
              if (!Array.isArray(value) || value.length < 1) {
                callback(new Error("启用会员前请至少选择一个会员类型"));
                return;
              }
              callback();
            },
            trigger: "change",
          },
        ],
        expiresAt: [
          {
            validator: (rule, value, callback) => {
              if (this.modal.type === "add") {
                if (!value) {
                  callback(new Error("请选择会员过期时间"));
                  return;
                }
                callback();
                return;
              }
              if (this.formData.status !== "ENABLED") {
                callback();
                return;
              }
              if (!value) {
                callback(new Error("启用会员前请选择会员过期时间"));
                return;
              }
              callback();
            },
            trigger: "change",
          },
        ],
      },
      columns: [
        { title: "用户名", key: "username", minWidth: 120, show: true },
        { title: "公司名称", key: "companyName", minWidth: 180, show: true },
        { title: "联系人", key: "contactPerson", minWidth: 120, show: true },
        { title: "联系方式", key: "phone", minWidth: 130, show: true },
        { title: "邮箱", key: "email", minWidth: 200, show: true },
        { title: "业务类型", slot: "businessTypes", minWidth: 180, show: true },
        { title: "首次登录", slot: "firstLoginAt", minWidth: 180, show: true },
        { title: "最后登录", slot: "lastLoginAt", minWidth: 180, show: true },
        {
          title: "营业执照",
          slot: "businessLicense",
          minWidth: 180,
          show: true,
        },
        {
          title: "业绩证明",
          slot: "performanceFile",
          minWidth: 180,
          show: true,
        },
        { title: "过期时间", slot: "expiresAt", minWidth: 190, show: true },
        { title: "下载权限", slot: "download", minWidth: 100, show: true },
        { title: "状态", slot: "status", minWidth: 100, show: true },
        {
          title: "操作",
          slot: "action",
          minWidth: 320,
          align: "center",
          fixed: "right",
          show: true,
        },
      ],
    };
  },
  computed: {
    tableColumns() {
      return this.columns.filter((item) => item.show);
    },
  },
  mounted() {
    this.loadBusinessTypeOptions();
    this.getData();
  },
  methods: {
    loadBusinessTypeOptions() {
      listBusinessTypeOptions().then((res) => {
        this.businessTypeOptions = Array.isArray(res) ? res : [];
      });
    },
    getData() {
      if (this.loading) return;
      this.loading = true;
      listMembers()
        .then((res) => {
          this.allList = Array.isArray(res) ? res : [];
          this.total = this.allList.length;
          this.refreshPageData();
          this.loading = false;
        })
        .catch(() => {
          this.loading = false;
        });
    },
    refreshPageData() {
      const start = (this.current - 1) * this.limit;
      this.list = this.allList.slice(start, start + this.limit);
    },
    handleChangePage(page) {
      this.current = page;
      this.refreshPageData();
    },
    handleChangePageSize(size) {
      this.current = 1;
      this.limit = size;
      this.refreshPageData();
    },
    handleChangeTableSize(size) {
      this.tableSize = size;
    },
    handleFullscreen() {
      this.tableFullscreen = !this.tableFullscreen;
      if (this.tableFullscreen) {
        screenfull.request(this.$refs.card.$el);
      } else {
        screenfull.exit();
      }
    },
    handleAdd() {
      this.removeBusinessLicense();
      this.removePerformanceFile();
      this.formData = {
        id: null,
        username: "",
        phone: "",
        email: "",
        companyName: "",
        contactPerson: "",
        unifiedSocialCreditCode: "",
        realName: "",
        password: "",
        confirmPassword: "",
        businessTypeIds: [],
        expiresAt: null,
        canDownloadFile: false,
        status: "ENABLED",
        businessLicenseFileId: null,
        threeYearPerformanceFileId: null,
      };
      this.modal = {
        show: true,
        type: "add",
      };
      this.$nextTick(() => {
        if (this.$refs.memberForm) {
          this.$refs.memberForm.resetFields();
        }
      });
    },
    handleEdit(row) {
      getMemberDetail(row.id).then((res) => {
        this.formData = {
          id: res.id,
          username: res.username || "",
          phone: res.phone || "",
          email: res.email || "",
          companyName: res.companyName || "",
          contactPerson: res.contactPerson || "",
          unifiedSocialCreditCode: res.unifiedSocialCreditCode || "",
          realName: res.realName || "",
          password: "",
          confirmPassword: "",
          businessTypeIds: Array.isArray(res.businessTypes)
            ? res.businessTypes.map((item) => item.id)
            : [],
          expiresAt: this.parseBackendDateTime(res.expiresAt),
          canDownloadFile: !!res.canDownloadFile,
          status: res.status || "ENABLED",
          businessLicenseFileId: res.businessLicenseFileId || null,
          threeYearPerformanceFileId: res.threeYearPerformanceFileId || null,
        };
        this.modal = {
          show: true,
          type: "edit",
        };
        this.businessLicense = this.normalizeProfileFileFromResponse(
          res,
          "business"
        );
        this.performanceFile = this.normalizeProfileFileFromResponse(
          res,
          "performance"
        );
        this.$nextTick(() => {
          if (this.$refs.memberForm) {
            this.$refs.memberForm.clearValidate();
          }
        });
      });
    },
    handleCloseModal() {
      this.modal.show = false;
    },
    apiErrorMessage(err) {
      if (!err) return "";
      const data = err.response && err.response.data;
      if (data && typeof data.message === "string" && data.message) {
        return data.message;
      }
      if (typeof err.message === "string" && err.message) {
        return err.message;
      }
      return "";
    },
    onMemberStatusChange() {
      this.$nextTick(() => {
        if (!this.$refs.memberForm) return;
        this.$refs.memberForm.validateField("businessTypeIds");
        this.$refs.memberForm.validateField("expiresAt");
      });
    },
    buildMemberUpdatePayload() {
      const fd = this.formData;
      const payload = {
        phone: (fd.phone || "").trim(),
        email: (fd.email || "").trim(),
        companyName: (fd.companyName || "").trim(),
        contactPerson: (fd.contactPerson || "").trim(),
        unifiedSocialCreditCode: (fd.unifiedSocialCreditCode || "")
          .trim()
          .toUpperCase(),
        realName: fd.realName != null ? String(fd.realName).trim() : "",
        status: fd.status,
      };
      const bt = Array.isArray(fd.businessTypeIds) ? fd.businessTypeIds : [];
      if (fd.status === "ENABLED" || bt.length > 0) {
        payload.businessTypeIds = bt;
      }
      if (fd.status === "ENABLED") {
        payload.expiresAt = this.formatDateTime(fd.expiresAt);
      } else if (fd.expiresAt) {
        payload.expiresAt = this.formatDateTime(fd.expiresAt);
      }
      return payload;
    },
    handleSubmit() {
      this.$refs.memberForm.validate((valid) => {
        if (!valid) return;
        this.submitting = true;
        const isAdd = this.modal.type === "add";
        const request = isAdd
          ? createMember({
              username: this.formData.username,
              phone: this.formData.phone,
              email: this.formData.email,
              companyName: this.formData.companyName,
              contactPerson: this.formData.contactPerson,
              unifiedSocialCreditCode: (
                this.formData.unifiedSocialCreditCode || ""
              )
                .trim()
                .toUpperCase(),
              realName: this.formData.realName,
              password: this.formData.password,
              confirmPassword: this.formData.confirmPassword,
              businessLicenseFileId: this.formData.businessLicenseFileId,
              threeYearPerformanceFileId:
                this.formData.threeYearPerformanceFileId,
              businessTypeIds: this.formData.businessTypeIds,
              expiresAt: this.formatDateTime(this.formData.expiresAt),
              canDownloadFile: this.formData.canDownloadFile,
              status: this.formData.status,
            })
          : updateMember(
              this.formData.id,
              this.buildMemberUpdatePayload()
            ).then(() =>
              updateMemberDownloadAccess(this.formData.id, {
                canDownloadFile: !!this.formData.canDownloadFile,
              })
            );
        request
          .then(() => {
            this.submitting = false;
            this.modal.show = false;
            this.$Message.success(
              isAdd ? "新增会员成功" : "编辑会员成功"
            );
            this.current = 1;
            this.getData();
          })
          .catch(() => {
            this.submitting = false;
          });
      });
    },
    // 文件上传：营业执照（单个）
    handleBeforeUploadBusinessLicense(file) {
      uploadMemberProfileFiles([file])
        .then((res) => {
          const files = Array.isArray(res) ? res : [];
          if (!files.length) return;
          const item = files[0];
          if (!item || item.fileId == null) return;
          this.formData.businessLicenseFileId = item.fileId;
          this.businessLicense = {
            fileId: item.fileId,
            fileName: item.fileName,
            contentType: item.contentType || file.type,
            fileSize: item.fileSize || file.size,
            thumbnailUrl: item.thumbnailUrl,
            thumbnailContentType: item.thumbnailContentType || "",
            thumbnailStatus: item.thumbnailStatus,
            localUrl: window.URL.createObjectURL(file),
          };
          this.$Message.success("营业执照上传成功");
        })
        .catch((err) => {
          const msg = this.apiErrorMessage(err);
          if (msg) this.$Message.error(msg);
        });
      return false;
    },
    // 文件上传：近三年业绩证明（单个）
    handleBeforeUploadPerformance(file) {
      uploadMemberProfileFiles([file])
        .then((res) => {
          const files = Array.isArray(res) ? res : [];
          if (!files.length) return;
          const item = files[0];
          if (!item || item.fileId == null) return;
          this.formData.threeYearPerformanceFileId = item.fileId;
          this.performanceFile = {
            fileId: item.fileId,
            fileName: item.fileName,
            contentType: item.contentType || file.type,
            fileSize: item.fileSize || file.size,
            thumbnailUrl: item.thumbnailUrl,
            thumbnailContentType: item.thumbnailContentType || "",
            thumbnailStatus: item.thumbnailStatus,
            localUrl: window.URL.createObjectURL(file),
          };
          this.$Message.success("业绩证明上传成功");
        })
        .catch((err) => {
          const msg = this.apiErrorMessage(err);
          if (msg) this.$Message.error(msg);
        });
      return false;
    },
    removeBusinessLicense() {
      if (this.businessLicense && this.businessLicense.localUrl) {
        window.URL.revokeObjectURL(this.businessLicense.localUrl);
      }
      this.formData.businessLicenseFileId = null;
      this.businessLicense = null;
    },
    removePerformanceFile() {
      if (this.performanceFile && this.performanceFile.localUrl) {
        window.URL.revokeObjectURL(this.performanceFile.localUrl);
      }
      this.formData.threeYearPerformanceFileId = null;
      this.performanceFile = null;
    },
    normalizeProfileFileFromResponse(source, type) {
      if (!source) return null;
      const prefix = type === "performance" ? "threeYearPerformance" : "businessLicense";
      const fileId = source[`${prefix}FileId`];
      if (!fileId) return null;
      return {
        fileId,
        fileName:
          source[`${prefix}FileName`] ||
          (type === "performance" ? "近三年业绩证明" : "营业执照"),
        contentType: source[`${prefix}ContentType`] || "",
        fileSize: source[`${prefix}FileSize`] || 0,
        thumbnailUrl: source[`${prefix}ThumbnailUrl`] || "",
        thumbnailContentType: source[`${prefix}ThumbnailContentType`] || "",
        thumbnailStatus: source[`${prefix}ThumbnailStatus`] || "",
      };
    },
    formatBusinessTypes(items) {
      if (!Array.isArray(items) || !items.length) return "—";
      return items.map((item) => item.name).join("、");
    },
    parseBackendDateTime(value) {
      if (!value) return null;
      if (value instanceof Date) return value;
      // 后端返回 LocalDateTime 通常是 `yyyy-MM-dd HH:mm:ss`，把空格替换成 `T` 以便浏览器解析
      const parsed = new Date(String(value).replace(" ", "T"));
      return Number.isNaN(parsed.getTime()) ? null : parsed;
    },
    formatDateTime(value) {
      if (!value) return "";
      const date =
        value instanceof Date ? value : this.parseBackendDateTime(value);
      if (!date) return "";
      const pad = (num) => String(num).padStart(2, "0");
      return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(
        date.getDate()
      )} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(
        date.getSeconds()
      )}`;
    },
    formatDisplayDate(value) {
      if (!value) return "—";
      const str = this.formatDateTime(value);
      return str || "—";
    },
    formatFileSize(size) {
      if (!size || Number.isNaN(size)) return "";
      if (size < 1024) return `${size} B`;
      if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
      if (size < 1024 * 1024 * 1024)
        return `${(size / 1024 / 1024).toFixed(1)} MB`;
      return `${(size / 1024 / 1024 / 1024).toFixed(1)} GB`;
    },
    formatThumbnailStatus(status) {
      if (!status) return "";
      if (status === "READY") return "可预览";
      if (status === "UNSUPPORTED") return "文件类型封面";
      if (status === "FAILED") return "预览生成失败";
      if (status === "PROCESSING") return "预览生成中";
      return status;
    },
    resolvePreviewUrl(file) {
      if (!file || !file.fileId) return "";
      return this.withApiBase(`/api/files/${file.fileId}/thumbnail`);
    },
    async previewProfileFile(file, fallbackName) {
      if (!file || !file.fileId) {
        this.$Message.info("暂无可预览的文件");
        return;
      }
      if (file.localUrl) {
        window.open(file.localUrl, "_blank");
        return;
      }
      const previewUrl = this.resolvePreviewUrl(file);
      if (!previewUrl) {
        this.$Message.error(`查看${fallbackName || "文件"}失败`);
        return;
      }
      window.open(previewUrl, "_blank");
    },
    withApiBase(path) {
      if (!path) return "";
      if (/^https?:\/\//i.test(path)) return path;
      const raw = String(path);
      if (raw.startsWith("/api/")) return raw;
      const base = (Setting.apiBaseURL || "").replace(/\/+$/, "");
      const cleaned = raw.replace(/^\/+/, "");
      return `${base}/${cleaned}`;
    },
    toggleStatus(row) {
      const status = row.status === "ENABLED" ? "DISABLED" : "ENABLED";
      updateMemberStatus(row.id, { status }).then(() => {
        this.$Message.success("状态更新成功");
        this.getData();
      });
    },
    toggleDownload(row) {
      const canDownloadFile = !row.canDownloadFile;
      updateMemberDownloadAccess(row.id, { canDownloadFile }).then(() => {
        this.$Message.success("下载权限更新成功");
        this.getData();
      });
    },
    resetPassword(row) {
      this.resetPasswordValue = "";
      this.$Modal.confirm({
        title: `重置 ${row.username} 的密码`,
        render: (h) =>
          h("Input", {
            props: {
              value: this.resetPasswordValue,
              type: "password",
              placeholder: "请输入新密码（至少6位）",
            },
            on: {
              input: (val) => {
                this.resetPasswordValue = val;
              },
            },
          }),
        onOk: () => {
          const password = (this.resetPasswordValue || "").trim();
          if (password.length < 6) {
            this.$Message.error("密码长度至少6位");
            return Promise.reject(new Error("invalid password"));
          }
          return resetMemberPassword(row.id, {
            password,
            confirmPassword: password,
          }).then(() => {
            this.$Message.success("重置密码成功");
          });
        },
      });
    },
    removeMember(row) {
      if (!row || !row.id) return;
      this.$Modal.confirm({
        title: "删除会员",
        content: `确认删除会员「${row.username || row.id}」吗？删除后不可恢复。`,
        okText: "确认删除",
        cancelText: "取消",
        onOk: () =>
          deleteMember(row.id).then(() => {
            this.$Message.success("删除会员成功");
            this.current = 1;
            this.getData();
          }),
      });
    },
  },
};
</script>

<style scoped>
.attachment-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 0;
  border-bottom: 1px dashed #f0f0f0;
}

.attachment-text {
  flex: 1;
  min-width: 0;
}

.attachment-name {
  font-weight: 600;
  color: #17233d;
}

.attachment-meta {
  margin-top: 4px;
  color: #999;
  font-size: 12px;
}

.attachment-actions {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.table-file-meta {
  color: #999;
  font-size: 12px;
  margin-top: 2px;
}
</style>
