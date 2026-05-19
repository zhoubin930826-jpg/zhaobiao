<template>
  <div class="contact-float">
    <button
      type="button"
      class="contact-trigger"
      aria-label="联系我们"
      :aria-describedby="popoverId"
    >
      <svg class="contact-icon" viewBox="0 0 24 24" aria-hidden="true">
        <path
          fill="currentColor"
          d="M12 2C6.48 2 2 6.02 2 10.9c0 2.55 1.3 4.83 3.35 6.35L4 22l5.28-1.32A9.86 9.86 0 0 0 12 19.8c5.52 0 10-4.02 10-8.9S17.52 2 12 2zm0 14.8a8.1 8.1 0 0 1-4.07-1.1l-.29-.17-2.57.64.69-2.5-.19-.3A6.7 6.7 0 0 1 4 10.9C4 7.12 7.58 4 12 4s8 3.12 8 6.9-3.58 6.9-8 6.9z"
        />
      </svg>
      <span class="contact-label">联系我们</span>
    </button>
    <div :id="popoverId" class="contact-popover" role="tooltip">
      <img :src="contactQrcodeUrl" alt="联系我们二维码" class="contact-qrcode" />
      <p class="contact-popover-tip">扫码联系我们</p>
    </div>
  </div>
</template>

<script setup>
import contactQrcodeUrl from '@/assets/contact-qrcode.png'

const popoverId = 'contact-qrcode-popover'
</script>

<style scoped>
.contact-float {
  position: fixed;
  right: 1.25rem;
  bottom: 1.25rem;
  z-index: 90;
}

.contact-trigger {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.55rem 0.85rem;
  border: 1px solid rgba(26, 95, 180, 0.25);
  border-radius: 999px;
  background: #fff;
  color: var(--color-primary, #1a5fb4);
  font-size: 0.82rem;
  line-height: 1;
  box-shadow: 0 4px 16px rgba(15, 23, 42, 0.12);
  transition: color 0.2s, border-color 0.2s, box-shadow 0.2s, transform 0.2s;
}

.contact-trigger:hover,
.contact-trigger:focus-visible {
  color: #fff;
  background: var(--color-primary, #1a5fb4);
  border-color: var(--color-primary, #1a5fb4);
  box-shadow: 0 6px 20px rgba(26, 95, 180, 0.35);
  outline: none;
}

.contact-icon {
  width: 1.15rem;
  height: 1.15rem;
  flex-shrink: 0;
}

.contact-label {
  white-space: nowrap;
}

.contact-popover {
  position: absolute;
  right: 0;
  bottom: calc(100% + 0.65rem);
  width: 252px;
  padding: 0.85rem;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 8px 28px rgba(15, 23, 42, 0.14);
  text-align: center;
  opacity: 0;
  visibility: hidden;
  transform: translateY(6px);
  transition: opacity 0.2s, transform 0.2s, visibility 0.2s;
  pointer-events: none;
}

.contact-popover::after {
  content: '';
  position: absolute;
  right: 1.25rem;
  bottom: -6px;
  width: 10px;
  height: 10px;
  background: #fff;
  border-right: 1px solid #e2e8f0;
  border-bottom: 1px solid #e2e8f0;
  transform: rotate(45deg);
}

.contact-float:hover .contact-popover,
.contact-float:focus-within .contact-popover {
  opacity: 1;
  visibility: visible;
  transform: translateY(0);
}

.contact-qrcode {
  display: block;
  width: 220px;
  height: auto;
  max-width: 100%;
  margin: 0 auto;
  object-fit: contain;
  border-radius: 6px;
  background: #f8fafc;
}

.contact-popover-tip {
  margin: 0.45rem 0 0;
  font-size: 0.75rem;
  color: #64748b;
}

@media (max-width: 480px) {
  .contact-label {
    display: none;
  }

  .contact-trigger {
    padding: 0.65rem;
    border-radius: 50%;
  }

  .contact-float {
    right: 0.85rem;
    bottom: 0.85rem;
  }
}
</style>
