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
              <el-divider content-position="left">等待触发任务</el-divider>
              <div>
                <div class="main-text">#12 测试任务（固定延时）<span class="main-text-min">2023-10-10 19:17:40</span></div>
                <div class="main-text">#13 测试任务（固定延时）<span class="main-text-min">2023-10-10 19:17:40</span></div>
              </div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="main-content">
              <el-divider content-position="left">调度趋势图</el-divider>
              <div>
                <div class="main-text">#1953 测试任务（固定延时）</div>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-main>
      <el-footer>
        <el-table
          :data="clusters"
          border
          style="width: 100%"
        >
          <el-table-column
            prop="date"
            label="地址"
            width="180"
          />
          <el-table-column
            prop="name"
            label="状态"
            width="180"
          />
          <el-table-column
            prop="address"
            label="角色"
          />
        </el-table>
      </el-footer>
    </el-container>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { baseCount } from '@/api/home'

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
      clusters: []
    }
  },
  computed: {
    ...mapGetters([
      'roles'
    ])
  },
  created() {
    this.queryBaseCount()
  },
  methods: {
    queryBaseCount() {
      baseCount().then(res => {
        if (res.code === 200) {
          this.baseCount = res.data
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
  width: 100%;
  padding: 50px;
}
.main-text{
  line-height: 25px;
  color: #303133;
}
.main-text-min {
  color: #909399;
  font-size: 13px;
}
</style>
