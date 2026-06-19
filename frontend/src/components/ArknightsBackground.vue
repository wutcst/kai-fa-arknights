<template>
  <div class="arknights-bg">
    <iframe
      src="/arknights-login/index.html"
      class="bg-iframe"
      frameborder="0"
    ></iframe>
  </div>
</template>

<script>
export default {
  name: 'ArknightsBackground'
};
</script>

<style scoped>
.arknights-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  z-index: 0;
  overflow: hidden;
}

.bg-iframe {
  width: 100vw;
  height: 100vh;
  border: none;
}
</style>
