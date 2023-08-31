<template>
  <div class="dashboard-container">
    <el-container>
      <el-header height="150px">
        <el-row>
          <el-col :span="6">
            <el-card shadow="always" class="card">
              <div class="card-item card-item-title">总任务</div>
              <div class="card-item card-item-value">{{ baseCount.totalJobNum }}</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="always" class="card">
              <div class="card-item card-item-title">执行中实例</div>
              <div class="card-item card-item-value">{{ baseCount.runningInstanceNum }}</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="always" class="card">
              <div class="card-item card-item-title">执行器</div>
              <div class="card-item card-item-value">{{ baseCount.workerNum }}</div>
            </el-card>
          </el-col>
          <el-col :span="6">
            <el-card shadow="always" class="card">
              <div class="card-item card-item-title">执行节点</div>
              <div class="card-item card-item-value">{{ baseCount.workerNodeNum }}</div>
            </el-card>
          </el-col>
        </el-row>
      </el-header>
      <el-main>
        <el-row>
          <el-col :span="12">
            <div class="main-content">
              <el-divider content-position="left">等待调度任务</el-divider>
              <div>
                <div v-for="item in waitingJobs" :key="item.id" class="main-text">
                  #{{ item.id }}&nbsp;{{ item.name }}
                  <span class="main-text-min">
                    {{ item.nextTriggerTime0 }}
                  </span>
                </div>
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="main-content">
              <el-divider content-position="left">调度中心集群节点</el-divider>
              <div>
                <el-table
                  :data="clusters"
                  style="width: 100%"
                >
                  <el-table-column
                    prop="address"
                    label="节点地址"
                  />
                  <el-table-column
                    label="状态"
                  >
                    <template v-slot="{ row }">
                      <span v-if="row.state"> <svg-icon icon-class="online" />上线</span>
                      <span v-else> <svg-icon icon-class="offline" />离线</span>
                    </template>
                  </el-table-column>
                  <el-table-column
                    label="Leader"
                  >
                    <template v-slot="{ row }">
                      {{ row.leader ? '是' : '否' }}
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-main>
    </el-container>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { baseCount, listWaitingJobs, clusters } from '@/api/home'

export default {
  name: 'Dashboard',
  data() {
    return {
      baseCount: {
        totalJobNum: 0,
        runningInstanceNum: 0,
        workerNum: 0,
        workerNodeNum: 0
      },
      clusters: [],
      waitingJobs: []
    }
  },
  computed: {
    ...mapGetters([
      'roles'
    ])
  },
  created() {
    this.$nextTick(() => {
      this.queryBaseCount()
      this.queryWaitingJobs()
      this.queryClusters()
    })
  },
  methods: {
    queryBaseCount() {
      baseCount().then(res => {
        if (res.code === 200) {
          this.baseCount = res.data
        }
      })
    },
    queryWaitingJobs() {
      listWaitingJobs().then(res => {
        if (res.code === 200) {
          this.waitingJobs = res.data
        }
      })
    },
    queryClusters() {
      clusters().then(res => {
        if (res.code === 200) {
          this.clusters = res.data
        }
      })
    }
  }
}
</script>

<style scoped>
.dashboard-container{
  padding: 20px;
}
.card {
  width: 90%;
  height: 100px;
  margin: 10px auto;
}
.card-item {
  text-align: center;
}
.card-item-title {
  font-size: large;
}
.card-item-value {
  line-height: 50px;
  font-size: 30px;
  font-weight: lighter;
}
.main-content {
  width: 70%;
  padding: 50px;
}
.main-text{
  padding: 10px;
  line-height: 25px;
  color: #303133;
  margin-top: 10px;
  background-color: #fafafa;
  border-radius: 7px;
}
.main-text-min {
  display: block;
  color: #909399;
  font-size: 13px;
}
</style>
