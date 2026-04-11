<template>
  <div>
      <div class="ivu-inline-block">
        <slot name='table-header'>
          <Button style='visibility:hidden'></Button>
        </slot>
      </div>
      <div v-if='!table.disabledDropdown' class="ivu-inline-block ivu-fr">
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
      v-bind='$attrs'
      ref="table"
      :columns="tableColumns"
      :data="table.list"
      :loading="table.loading"
      :size="tableSize"
      :on-select-change='handleSelectChange'
      class="ivu-mt"
    >
      <template v-for="item in slotKeys" slot-scope="{ row }" :slot="item.key">
        <slot v-bind="row" :name="item.key"></slot>
      </template>
    </Table>
    <div v-if='!table.disabledPagination' class="ivu-mt ivu-text-right">
      <Page
        :total="table.total"
        :current="current"
        show-total
        show-sizer
        show-elevator
        :page-size="limit"
        @on-change="handleChangePage"
        @on-page-size-change="handleChangePageSize"
      />
    </div>
  </div>
</template>
<script>
    import screenfull from 'screenfull';

    export default {
        props: {
            el: {
                type: Object,
                default: null
            },
            columns: {
                type: Array,
                default: () => []
            },
            table: {
                type: Object,
                default: null
            }

        },
        data () {
            return {
                list: [],
                selectedData: [],
                current: 1,
                limit: 10,
                sortType: 'normal', // 当前排序类型，可选值为：normal（默认） || asc（升序）|| desc（降序）,
                sortKey: '', // 排序列的 key
                tableSize: 'default',
                tableFullscreen: false,
                submitting: false
            };
        },
        computed: {
            // 动态设置列
            tableColumns () {
                const columns = [...this.columns];
                return columns.filter((item) => item.show);
            },
            offset () {
                return (this.current - 1) * this.limit;
            },
            slotKeys () {
                return this.columns.filter((item) => !!item.slot)
            }
        },
        methods: {
            // 改变表格尺寸
            handleChangeTableSize (size) {
                this.tableSize = size;
            },
            // 表格全屏
            handleFullscreen () {
                this.tableFullscreen = !this.tableFullscreen;
                // this.$emit('on-fullscreen', this.tableFullscreen);
                if (this.tableFullscreen) {
                    screenfull.request(this.el);
                } else {
                    screenfull.exit();
                }
            },
            // 刷新表格数据
            handleRefresh () {
                this.$emit('getData')
            },
            // handleSelectChange
            handleSelectChange (selection) {
                this.selectedData = selection
            },
            // 切换页码
            handleChangePage (page) {
                this.current = page;
                this.$emit('getData', {
                    page
                })
            },
            // 切换每页条数
            handleChangePageSize (size) {
                console.log('size')
                this.current = 1;
                this.limit = size;
                this.$emit('getData', {
                    pagesize: size,
                    page: 1
                })
            }
        }
    };
</script>

<style lang="less" scoped></style>
