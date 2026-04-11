<template>
    <div>
        <Row :gutter="24">
            <Col :xl="7" :lg="7" :md="24" :sm="24" :xs="24" class="ivu-mb">
                <Card :bordered="false" dis-hover>
                    <div class="ivu-text-center">
                        <Avatar :src="user.avatar || 'http://file.zhiliandun.cn/81f8a509gy1fnjdvkkwgoj20zk0m8ak8.jpg'" class="setting-user-avatar" />
                        <p class="ivu-m-8">
                            <strong v-font="20">{{ user.realName || user.nickname || user.username }}</strong>
                        </p>
                        <p>{{ user.username }}</p>
                    </div>
                    <!-- <Divider dashed /> -->
                </Card>
            </Col>
        </Row>
    </div>
</template>
<script>
    import { getUserInfo } from '@api/account';
    export default {
        name: 'setting-user',
        // components: { articleTemplate, projectTemplate, appTemplate },
        data () {
            return {
                user: {
                    name: 'Aresn',
                    avatar: 'https://dev-file.iviewui.com/userinfoPDvn9gKWYihR24SpgC319vXY8qniCqj4/avatar',
                    sign: '满招损，谦受益。',
                    tags: [
                        '大神',
                        '最强王者',
                        '落地成盒',
                        '很有想法',
                        '懂点设计',
                        '菜烧的不错'
                    ]
                },
                icon: {
                },
                addIcon: false,
                newIcon: '',
                currentTab: 'article'
            }
        },
        mounted () {
            getUserInfo().then(res => {
                this.user = res
            })
        },
        methods: {
            handleOpenNewTag () {
                this.addIcon = true;
                this.$nextTick(() => {
                    this.$refs.tag.focus();
                });
            },
            handleAddNewTag () {
                if (this.newIcon) {
                    this.user.tags.push(this.newIcon);
                    this.newIcon = '';
                    this.addIcon = false;
                }
            }
        }
    }
</script>

<style lang="less" scoped>
    .setting-user{
        &-avatar{
            width: 72px;
            height: 72px;
            border-radius: 50%;
        }
    }
</style>
