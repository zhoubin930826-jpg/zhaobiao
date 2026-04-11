<template>
  <div>
    <h2>基本设置</h2>
    <Form
      ref="form"
      :model="data"
      :rules="rules"
      label-position="top"
      class="ivu-mt"
    >
      <Row type="flex" :gutter="48">
        <Col v-bind="grid2">
          <FormItem label="用户名">
            <Input v-model="data.username" disabled placeholder="用户名" />
          </FormItem>
          <FormItem label="真实姓名" prop="realName">
            <Input v-model="data.realName" placeholder="真实姓名" />
          </FormItem>
          <FormItem label="手机号" prop="phone">
            <Input v-model="data.phone" placeholder="手机号" />
          </FormItem>
          <FormItem label="邮箱" prop="email">
            <Input v-model="data.email" placeholder="邮箱" />
          </FormItem>
          <FormItem label="公司名称" prop="companyName">
            <Input v-model="data.companyName" placeholder="公司名称" />
          </FormItem>
          <FormItem label="联系人" prop="contactPerson">
            <Input v-model="data.contactPerson" placeholder="联系人" />
          </FormItem>
          <FormItem label="统一社会信用代码" prop="unifiedSocialCreditCode">
            <Input v-model="data.unifiedSocialCreditCode" placeholder="18 位代码" />
          </FormItem>
          <FormItem>
            <Button type="primary" @click="handleSubmit">保存</Button>
          </FormItem>
        </Col>
      </Row>
    </Form>
  </div>
</template>
<script>
    import { mapActions } from 'vuex';
    import { getUserInfo, updateProfile } from '@api/account';

    export default {
        mounted () {
            this.loadProfile();
        },
        data () {
            return {
                grid2: {
                    xl: { span: 12, order: 1 },
                    lg: { span: 24, order: 2 },
                    md: { span: 24, order: 2 },
                    sm: { span: 24, order: 2 },
                    xs: { span: 24, order: 2 }
                },
                data: {
                    username: '',
                    realName: '',
                    phone: '',
                    email: '',
                    companyName: '',
                    contactPerson: '',
                    unifiedSocialCreditCode: ''
                },
                rules: {
                    email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
                }
            };
        },
        methods: {
            ...mapActions('admin/user', ['set']),
            loadProfile () {
                getUserInfo().then(res => {
                    this.data = {
                        username: res.username || '',
                        realName: res.realName || '',
                        phone: res.phone || '',
                        email: res.email || '',
                        companyName: res.companyName || '',
                        contactPerson: res.contactPerson || '',
                        unifiedSocialCreditCode: res.unifiedSocialCreditCode || ''
                    };
                });
            },
            handleSubmit () {
                this.$refs.form.validate(valid => {
                    if (!valid) return;
                    const { username, ...request } = this.data;
                    updateProfile(request).then(async (profile) => {
                        const info = {
                            ...profile,
                            name: profile.realName || profile.username,
                            nickname: profile.realName || profile.username
                        };
                        await this.set(info);
                        this.$Message.success('保存成功');
                    });
                });
            }
        }
    };
</script>

<style lang="less" scoped>
.setting-account-info {
  &-avatar {
    width: 90px;
    height: 90px;
  }
}
</style>
