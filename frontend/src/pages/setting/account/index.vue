<template>
    <div>
        <Row :gutter="24">
            <Col :xl="6" :lg="6" :md="0" :sm="0" :xs="0">
                <Card shadow title="个人设置" icon="md-options" :padding="0">
                    <CellGroup @on-click="handleChangeCell" class="ivu-pt-8 ivu-pb-8">
                        <Cell title="基本设置" name="info" label="个人账户信息设置" :selected="currentType === 'info'" />
                        <!-- <Cell title="账号绑定" name="account" label="绑定第三方社交账户" :selected="currentType === 'account'" />
                        <Cell title="新消息通知" name="notification" label="各种通知的设置" :selected="currentType === 'notification'" /> -->
                    </CellGroup>
                </Card>
            </Col>
            <Col :xl="18" :lg="18" :md="24" :sm="24" :xs="24">
                <Card :bordered="false" dis-hover>
                    <info v-if="currentType === 'info'" />
                    <account v-if="currentType === 'account'" />
                    <!-- <notification v-if="currentType === 'notification'" /> -->
                </Card>
            </Col>
        </Row>
    </div>
</template>
<script>
    import info from './info';
    import account from './account';
    // import notification from './notification';

    export default {
        name: 'setting-account',
        components: { info, account },
        data () {
            return {
                currentType: 'info' // info | account | notification
            }
        },
        methods: {
            handleChangeCell (name) {
                this.currentType = name;
            }
        }
    }
</script>
