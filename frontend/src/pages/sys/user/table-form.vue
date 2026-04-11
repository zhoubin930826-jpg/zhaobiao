<template>
  <Form
    ref="form"
    :model="data"
    :rules="rules"
    :label-width="labelWidth"
    :label-position="labelPosition"
  >
    <Row :gutter="24" type="flex" justify="end">
      <Col v-bind="grid">
        <FormItem label="用户名：" prop="name" label-for="name">
          <Input
            v-model="data.name"
            placeholder="请输入用户名"
            element-id="name"
          />
        </FormItem>
      </Col>
      <Col v-bind="grid">
        <FormItem label="用户角色" prop="role_id" label-for="role_id">
          <Select
            v-model="data.role_id"
            placeholder="请选择角色"
            element-id="role_id"
          >
            <Option :value="item.id" v-for=" item in roleList" :key="item.id">{{item.name}}</Option>
          </Select>
        </FormItem>
      </Col>
      <Col v-bind="grid" class="ivu-text-right">
        <FormItem>
          <Button type="primary" @click="handleSubmit">查询</Button>
          <Button class="ivu-ml-8" @click="handleReset">重置</Button>
        </FormItem>
      </Col>
    </Row>
  </Form>
</template>
<script>
    import { mapState } from 'vuex';
    import { getRoleList } from '@api/system';

    export default {
        mounted () {
            getRoleList().then(res => {
                // console.log(res)
                this.roleList = res
            })
        },
        data () {
            return {
                grid: {
                    xl: 8,
                    lg: 8,
                    md: 12,
                    sm: 24,
                    xs: 24
                },
                roleList: [],
                data: {
                    name: '',
                    role_id: '',
                    phone: '',
                    mail: '',
                    gender: 0,
                    account: '',
                    id: '',
                    status: 0,
                    date: []
                },
                rules: {}
            };
        },
        computed: {
            ...mapState('admin/layout', ['isMobile']),
            labelWidth () {
                return this.isMobile ? undefined : 100;
            },
            labelPosition () {
                return this.isMobile ? 'top' : 'right';
            }
        },
        methods: {
            handleSubmit () {
                this.$emit('on-submit', this.data);
            },
            handleReset () {
                this.$refs.form.resetFields();
                this.$emit('on-reset');
            }
        }
    };
</script>
