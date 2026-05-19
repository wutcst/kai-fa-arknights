<template>
  <div id="app">
    <h1>World of Zuul</h1>
    <div class="game-container">
      <div class="message">{{ message }}</div>
      <div class="room-info">
        <h2>{{ currentRoom }}</h2>
        <p>{{ longDescription }}</p>
      </div>
      <div class="exits">
        <span>Exits: </span>
        <button v-for="exit in exits" :key="exit" @click="move(exit)">
          {{ exit }}
        </button>
      </div>
      <div class="help">
        <button @click="getHelp">Help</button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

const API_URL = 'http://localhost:8080/api/game';

export default {
  name: 'App',
  data() {
    return {
      message: 'Welcome to the World of Zuul!',
      currentRoom: '',
      longDescription: '',
      exits: []
    };
  },
  mounted() {
    this.fetchStatus();
  },
  methods: {
    async fetchStatus() {
      try {
        const response = await axios.get(`${API_URL}/status`);
        this.currentRoom = response.data.description;
        this.longDescription = response.data.longDescription;
        this.exits = Array.from(response.data.exits);
        this.message = '';
      } catch (error) {
        this.message = 'Error: Cannot connect to server. Is backend running?';
      }
    },
    async move(direction) {
      try {
        const response = await axios.post(`${API_URL}/move`, { direction });
        this.message = response.data.message;
        this.currentRoom = response.data.description;
        this.longDescription = response.data.longDescription;
        this.exits = Array.from(response.data.exits);
      } catch (error) {
        this.message = 'Error moving: ' + error.message;
      }
    },
    async getHelp() {
      try {
        const response = await axios.get(`${API_URL}/help`);
        this.message = response.data.message;
      } catch (error) {
        this.message = 'Error: ' + error.message;
      }
    }
  }
};
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
  max-width: 600px;
  margin: 60px auto;
}

.game-container {
  border: 2px solid #42b983;
  border-radius: 10px;
  padding: 20px;
  background-color: #f5f5f5;
}

.message {
  color: #e74c3c;
  font-weight: bold;
  min-height: 24px;
  margin-bottom: 10px;
}

.room-info {
  margin: 20px 0;
}

.exits {
  margin: 20px 0;
}

.exits button {
  margin: 5px;
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
}

.exits button:hover {
  background-color: #3aa876;
}

.help button {
  padding: 8px 16px;
  font-size: 14px;
  cursor: pointer;
  background-color: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
}

.help button:hover {
  background-color: #2980b9;
}
</style>
