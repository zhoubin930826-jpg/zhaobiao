<template>
  <footer class="footer">
    <ContactFloat />
    <div class="inner">
      <div class="footer-main">
        <div class="footer-brand">
          <p class="company-name">湖北省军科应力装备制造有限公司</p>
          <p class="company-line">
            <span class="line-label">地址：</span>
            <span>湖北省武汉市武汉经济技术开发区山科技产业园二区C栋写字楼4层C4016</span>
          </p>
          <p class="company-line">
            <span class="line-label">邮箱：</span>
            <a href="mailto:junkezhizao@163.com">junkezhizao@163.com</a>
          </p>
          <p class="company-line">
            <span class="line-label">电话：</span>
            <a href="tel:18427225774">18427225774</a>
          </p>
        </div>
      </div>
      <div class="footer-bottom">
        <p class="copyright">© {{ year }} 招投标信息公示</p>
        <span class="footer-divider">|</span>
        <p class="icp">
          <a href="https://beian.miit.gov.cn/" target="_blank" rel="noopener noreferrer">鄂ICP备2026020834号-1</a>
        </p>
      </div>
    </div>
  </footer>
</template>

<script setup>
import ContactFloat from '@/components/ContactFloat.vue'

const year = new Date().getFullYear()
</script>

<style scoped>
.footer {
  position: relative;
  margin-top: auto;
  background: #e8e8e8;
  border-top: 3px solid var(--color-nav, #015293);
  padding: 18px 0 14px;
  font-size: 13px;
  color: #666;
}

.inner {
  max-width: var(--content-width, 1100px);
  margin: 0 auto;
  padding: 0 12px;
}

.footer-main {
  padding-bottom: 12px;
  border-bottom: 1px solid #ccc;
}

.company-name {
  margin: 0 0 8px;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.company-line {
  margin: 0 0 4px;
  line-height: 1.7;
}

.line-label {
  color: #333;
}

.company-line a {
  color: #666;
  text-decoration: none;
}

.company-line a:hover {
  color: var(--color-link, #1277e0);
  text-decoration: underline;
}

.footer-bottom {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding-top: 12px;
  text-align: center;
}

.copyright,
.icp {
  margin: 0;
}

.footer-divider {
  color: #bbb;
}

.icp a {
  color: #666;
  text-decoration: none;
}

.icp a:hover {
  color: var(--color-link, #1277e0);
  text-decoration: underline;
}
</style>
