<template>
  <el-card header="Request images">
    <el-form inline @submit.prevent="submit">
      <el-form-item label="Animal">
        <el-select v-model="animalType" style="width: 120px">
          <el-option label="dog" value="DOG" />
          <el-option label="cat" value="CAT" />
        </el-select>
      </el-form-item>
      <el-form-item label="Count">
        <el-input-number v-model="count" :min="1" />
      </el-form-item>
      <el-button type="primary" native-type="submit" :loading="sending">Send</el-button>
    </el-form>
    <el-alert v-if="sending" :title="message" type="info" :closable="false" />
  </el-card>
</template>

<script>
import { ElMessageBox } from 'element-plus'
import { ImageServiceClient } from '../services/ImageServiceClient.js'
import messages from '../santa-messages.json'

export default {
  name: 'AnimalRequestForm',
  emits: ['submitted'],
  data () {
    return { animalType: 'DOG', count: 1, sending: false, messageIndex: 0 }
  },
  computed: {
    message () {
      return messages[this.messageIndex]
    }
  },
  created () {
    this.client = new ImageServiceClient()
  },
  methods: {
    async submit () {
      this.sending = true
      const timer = setInterval(this.nextMessage, 2000)
      try {
        await this.client.requestAnimals(this.animalType, this.count)
        this.$emit('submitted')
      } catch (e) {
        ElMessageBox.alert(e.message, 'Error', { type: 'error' })
      } finally {
        clearInterval(timer)
        this.sending = false
      }
    },
    nextMessage () {
      this.messageIndex = (this.messageIndex + 1) % messages.length
    }
  }
}
</script>
