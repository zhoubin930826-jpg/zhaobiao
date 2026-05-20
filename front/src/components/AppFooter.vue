<template>
  <footer class="footer">
    <ContactFloat />
    <div class="inner footer-inner">
      <div class="footer-logo">
        <img :src="logoUrl" alt="军科制造" class="footer-logo-img" />
      </div>
      <div class="footer-content">
        <p class="company-name">湖北省军科应力装备制造有限公司</p>
        <ul class="contact-list">
          <li>
            <span class="label">地址</span>
            <span class="value">湖北省武汉市武汉经济技术开发区军山科技产业园二区C栋写字楼4层C4016</span>
          </li>
          <li>
            <span class="label">邮箱</span>
            <a class="value link" href="mailto:junkezhizao@163.com">junkezhizao@163.com</a>
          </li>
          <li>
            <span class="label">电话</span>
            <a class="value link" href="tel:18427225774">18427225774</a>
          </li>
        </ul>
        <div class="legal-row">
          <span>© {{ year }} 招投标信息公示</span>
          <span class="sep" aria-hidden="true">·</span>
          <a
            href="https://beian.miit.gov.cn/"
            target="_blank"
            rel="noopener noreferrer"
          >鄂ICP备2026020834号-1</a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
import ContactFloat from '@/components/ContactFloat.vue'
import logoUrl from '@/assets/logo.jpg'

const year = new Date().getFullYear()
</script>

<style scoped>
.footer {
  position: relative;
  margin-top: auto;
  background: linear-gradient(180deg, #4a5568 0%, #374151 100%);
  border-top: 1px solid #5b6578;
  color: rgba(255, 255, 255, 0.9);
}

.footer-inner {
  display: flex;
  align-items: flex-end;
  gap: 1.25rem 1.5rem;
  max-width: var(--content-width, 1200px);
  margin: 0 auto;
  padding: 1.5rem 1.25rem 1.25rem;
}

.footer-logo {
  flex-shrink: 0;
  align-self: flex-end;
}

.footer-logo-img {
  display: block;
  max-height: 80px;
  width: auto;
  max-width: 120px;
  object-fit: contain;
  background: #fff;
  border-radius: 6px;
  padding: 0.35rem;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.35);
}

.footer-content {
  flex: 1;
  min-width: 0;
  text-align: center;
}

.company-name {
  margin: 0 0 0.75rem;
  font-size: 0.9rem;
  font-weight: 600;
  color: #f5f5f5;
  letter-spacing: 0.02em;
}

.contact-list {
  list-style: none;
  margin: 0 auto 1rem;
  padding: 0;
  max-width: 52rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.4rem;
}

.contact-list li {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: baseline;
  gap: 0.35rem 0.5rem;
  font-size: 0.8125rem;
  line-height: 1.6;
}

.label {
  color: #9ca3af;
}

.label::after {
  content: '：';
}

.value {
  color: #d1d5db;
}

.link {
  color: #d1d5db;
  text-decoration: none;
  transition: color 0.2s;
}

.link:hover {
  color: #fff;
  text-decoration: underline;
}

.legal-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 0.35rem 0.5rem;
  padding-top: 0.85rem;
  border-top: 1px solid #5b6578;
  font-size: 0.75rem;
  color: #9ca3af;
}

.legal-row a {
  color: #9ca3af;
  text-decoration: none;
  transition: color 0.2s;
}

.legal-row a:hover {
  color: #e5e7eb;
  text-decoration: underline;
}

.sep {
  opacity: 0.55;
  user-select: none;
}

@media (min-width: 768px) {
  .contact-list {
    flex-direction: row;
    flex-wrap: wrap;
    justify-content: center;
    gap: 0.5rem 1.5rem;
  }

  .contact-list li {
    flex-wrap: nowrap;
  }

  .contact-list li:first-child {
    flex: 1 1 100%;
  }
}

@media (max-width: 640px) {
  .footer-inner {
    flex-direction: column;
    align-items: flex-start;
  }

  .footer-logo-img {
    max-height: 64px;
    max-width: 100px;
  }

  .footer-content {
    width: 100%;
    text-align: left;
  }

  .contact-list {
    align-items: flex-start;
    margin-left: 0;
    margin-right: 0;
  }

  .contact-list li {
    justify-content: flex-start;
  }

  .legal-row {
    justify-content: flex-start;
  }
}
</style>
