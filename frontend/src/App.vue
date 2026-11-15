<template>
  <el-main style="max-width: 960px; margin: auto">
    <animal-request-form @submitted="$refs.browser.refresh()" />
    <image-browser ref="browser" style="margin-top: 20px" />
  </el-main>
</template>

<script>
import AnimalRequestForm from './components/AnimalRequestForm.vue'
import ImageBrowser from './components/ImageBrowser.vue'

export default {
  name: 'App',
  components: { AnimalRequestForm, ImageBrowser }
}
</script>
