<template>
  <div>
    <Button type="primary" ghost icon="md-add" @click="handleOpenCreate">添加用户</Button>

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
      <template slot-scope="{ row }" slot="action">
        <div @click.stop.prevent>
          <a type="text" @click="handleEdit(row.id)">编辑</a>
          <Divider type="vertical" />
          <a type="text" @click="handleDelete(row.id)">删除</a>
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
      :title="modal.type === 'edit' ? '编辑角色' : '添加角色'"
      :before-close="handleCloseEdit"
      :transfer="false"
      width="500"
    >
      <Form
        v-if="roleInfo && roleInfoReady"
        ref="roleInfoForm"
        :model="roleInfo"
        :rules="roleInfoRules"
        label-position="top"
        label-colon

      >
        <FormItem prop="name" label="角色名称">
          <Input v-model="roleInfo.name" placeholder="请输入角色名称，必填" />
        </FormItem>
        <FormItem prop="status" label="是否启用">
          <RadioGroup v-model="roleInfo.status">
            <Radio label="1">启用</Radio>
            <Radio label="0">停用</Radio>
          </RadioGroup>
        </FormItem>
        <FormItem prop="authIds" label="权限管理">
          <Tree
            :data="menuList"
            show-checkbox
            ref="tree"
            :children-key="`child`"
            @on-select-change="change"
          />
        </FormItem>
        <FormItem prop="organize_id" label="所属组织">
          <el-cascader
            :options="orgList"
            :props="{ checkStrictly: true,expandTrigger: 'hover' }"
            clearable
            v-model="roleInfo.organize_id"
          ></el-cascader>
        </FormItem>
      </Form>
      <template #footer>
        <Button v-if="modal.type === 'edit'" type="primary" @click="handleSubmitEdit" :loading="submitting">提交</Button>
        <Button v-if="modal.type === 'new'" type="primary" @click="handleSubmitNew" :loading="submitting">提交</Button>
      </template>
    </Modal>
  </div>
</template>

<script>
    import { listRoles } from '@api/system';
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
                        title: '操作',
                        key: 'action',
                        slot: 'action',
                        minWidth: 200,
                        align: 'center',
                        fixed: 'right',
                        show: true
                    }
                ],
                orgList: [],
                loading: false,
                list: [],
                current: 1,
                limit: 10,
                total: 0,
                tableSize: 'default',
                tableFullscreen: false,
                modal: {
                    show: false,
                    type: 'edit' // edit || new
                },
                roleInfo: null,
                roleInfoRules: {
                    name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
                    // authIds: [{ required: true, message: '请选择权限', trigger: 'blur' }],
                    status: [{ required: true, message: '请选择启用状态', trigger: 'blur' }]
                    // organize_id: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
                },
                roleInfoString: '',
                roleInfoReady: true,
                submitting: false,
                menuList: [],
                roleMenuList: []
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
            }
        },
        filters: {
            timeFilter (val) {
                console.log(val)
                return new Date(val - 0).toLocaleDateString() + new Date(val - 0).toLocaleTimeString()
            },
            filters: {
                filterPer (val) {
                    val.forEach(item => {
                        if (item.children.length > 0) {
                            item.checked = false;
                        }
                    });
                    return val;
                }
            }
        },
        methods: {
            getData () {
                listRoles().then(res => {
                    const arr = Array.isArray(res) ? res : [];
                    this.list = arr;
                    this.total = arr.length;
                });
            },
            change () {
                // console.log(this.menu_list);
            },
            // 查找单一角色信息
            handleGetRole (id) {
                return this.list.find(item => item.id === id);
            },
            // 删除
            handleDelete (id) {
            },
            // 编辑
            handleEdit (id) {
            },
            handleInitRoleInfoForm () {
            },
            // 关闭编辑
            handleCloseEdit () {
            },
            // 添加用户
            handleOpenCreate () {
            },
            handleSubmitEdit () {
            },
            handleSubmitNew () {

            },
            // 获取菜单数据
            handleGetMenuList () {
            },
            // 获取组织机构
            handleGetOrgList () {
            }
        },
        mounted () {
            this.getData()
        }
    };
</script>

<style lang="less" scoped></style>
