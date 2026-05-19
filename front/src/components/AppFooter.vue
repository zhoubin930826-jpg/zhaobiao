<template>
  <footer class="footer">
    <ContactFloat />
    <div class="inner">
      <div class="company-info">
        <p><span class="label">名&emsp;&emsp;称：</span>湖北省军科应力装备制造有限公司</p>
        <p><span class="label">地&emsp;&emsp;址：</span>湖北省武汉市武汉经济技术开发区山科技产业园二区C栋写字楼4层C4016</p>
        <p>
          <span class="label">联系邮箱：</span>
          <a href="mailto:junkezhizao@163.com" class="contact-link">junkezhizao@163.com</a>
        </p>
        <p>
          <span class="label">联系方式：</span>
          <a href="tel:18427225774" class="contact-link">18427225774</a>
        </p>
      </div>
      <p class="muted">© {{ year }} 招投标信息公示</p>
      <p class="icp">
        <a href="https://beian.miit.gov.cn/" target="_blank" rel="noopener noreferrer">鄂ICP备2026020834号-1</a>
      </p>
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
  border-top: 1px solid #e2e8f0;
  background: linear-gradient(180deg, #fbfcfe 0%, #f1f5f9 100%);
  padding: 1.35rem 1.25rem 1.5rem;
  margin-top: auto;
  box-shadow: 0 -1px 0 rgba(255, 255, 255, 0.8) inset;
}

.inner {
  max-width: 1200px;
  margin: 0 auto;
  text-align: center;
  font-size: 0.84rem;
  color: #64748b;
}

.inner p {
  margin: 0.25rem 0;
}

.company-info {
  margin-bottom: 0.75rem;
  font-size: 0.85rem;
  color: #475569;
}

.company-info p {
  margin: 0.35rem 0;
}

.label {
  display: inline-block;
  color: #64748b;
}

.contact-link {
  color: #475569;
  text-decoration: none;
  border-bottom: 1px dashed rgba(71, 85, 105, 0.5);
}

.contact-link:hover {
  color: #1d4ed8;
  border-bottom-color: rgba(29, 78, 216, 0.65);
}

.muted {
  font-size: 0.8rem;
  opacity: 0.85;
}

.icp {
  font-size: 0.78rem;
  color: #64748b;
  opacity: 0.9;
}

.icp a {
  color: inherit;
  text-decoration: none;
  border-bottom: 1px dashed rgba(100, 116, 139, 0.65);
}

.icp a:hover {
  color: #1d4ed8;
  border-bottom-color: rgba(29, 78, 216, 0.65);
}
</style>
