<template>
  <el-card header="Images">
    <el-table v-loading="loading" :data="images">
      <el-table-column prop="createdAt" label="Created" />
      <el-table-column prop="source" label="Source" />
      <el-table-column label="Image">
        <template #default="{ row }">
          <el-link type="primary" @click="openImage(row)">View</el-link>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="viewerVisible" :title="viewerTitle" @closed="releaseImage">
      <img v-if="imageUrl" :src="imageUrl" style="max-width: 100%" />
    </el-dialog>
  </el-card>
</template>

<script>
import { ElMessageBox } from 'element-plus'
import { ImageServiceClient } from '../services/ImageServiceClient.js'

export default {
  name: 'ImageBrowser',
  data () {
    return { images: [], loading: false, viewerVisible: false, viewerTitle: '', imageUrl: null }
  },
  created () {
    this.client = new ImageServiceClient()
  },
  mounted () {
    this.refresh()
  },
  methods: {
    async refresh () {
      this.loading = true
      try {
        this.images = await this.client.listImages()
      } catch (e) {
        this.showError(e)
      } finally {
        this.loading = false
      }
    },
    async openImage (image) {
      try {
        this.imageUrl = URL.createObjectURL(await this.client.fetchImage(image.id))
        this.viewerTitle = image.source
        this.viewerVisible = true
      } catch (e) {
        this.showError(e)
      }
    },
    releaseImage () {
      URL.revokeObjectURL(this.imageUrl)
      this.imageUrl = null
    },
    showError (e) {
      ElMessageBox.alert(e.message, 'HTTP error', { type: 'error' })
    }
  }
}
</script>
