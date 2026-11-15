export class ImageServiceClient {
  constructor (baseUrl = '/api') {
    this.baseUrl = baseUrl
  }

  async requestAnimals (animalType, count) {
    await this.#send('/animals/requests', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ animalType, count })
    })
  }

  async listImages () {
    const response = await this.#send('/images')
    return response.json()
  }

  async fetchImage (id) {
    const response = await this.#send(`/images/${id}/content`)
    return response.blob()
  }

  async #send (path, options) {
    const response = await fetch(this.baseUrl + path, options)
    if (!response.ok) {
      throw new Error(`HTTP ${response.status} ${path} : ${await response.text()}`)
    }
    return response
  }
}
