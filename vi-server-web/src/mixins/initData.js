export default {
  data() {
    return {
      // 表格数据
      data: [],
      // 页码
      page: 1,
      // 每页数据条数
      limit: 10,
      // 总数据条数
      total: 0,
      // 查询数据的参数
      params: {},
      // 待查询的对象
      query: {},
      // 调用方法
      invoke: undefined,
      // 等待时间
      time: 50,
      // 表格 Loading 属性
      loading: true,
      // 选择条数
      selNum: 0,
      // 选择项
      selections: [],
      pickerOptions: dateTimeOps
    }
  },
  computed: {
    // 选择项不为1条
    notOne() {
      return this.selections.length !== 1
    },
    // 选择项不为1条或多条
    notMany() {
      return this.selections.length < 1
    }
  },
  methods: {
    async init() {
      if (!this.beforeInit()) {
        return
      }
      return new Promise((resolve, reject) => {
        this.loading = true
        this.invoke(this.getQueryParams()).then(res => {
          if (res.code === 200) {
            this.total = res.data.total
            this.data = res.data.records
          } else {
            this.failTips(res.message)
          }
          setTimeout(() => {
            this.loading = false
          }, this.time)
          resolve(res)
        }).catch(err => {
          this.loading = false
          reject(err)
        })
      })
    },
    beforeInit() {
      return true
    },
    pageChangeHandler(e) {
      this.page = e
      this.init()
    },
    sizeChangeHandler(e) {
      this.page = 1
      this.size = e
      this.init()
    },
    getQueryParams() {
      this.params = { ...this.query }
      // 时间查询-selectTime
      if (this.params.selectTime) {
        this.params.startTime = this.query.selectTime[0]
        this.params.endTime = this.query.selectTime[1]
        delete this.params.selectTime
      }
      return {
        page: this.page,
        limit: this.limit,
        ...this.params
      }
    },
    toQuery() {
      this.page = 1
      this.init()
    },
    resetQuery() {
      this.query = {}
      this.params = {}
      this.init()
    },
    // 选择项
    selectionChange(selection) {
      this.selections = selection
    },
    // 批量删除
    toDel(delMethod) {
      if (!this.selections || this.selections.length === 0) {
        return
      }
      const ids = []
      this.selections.forEach(e => ids.push(e.id))
      this.confirm('确认删除' + this.selections.length + '条记录吗?', ids, p => {
        delMethod(ids).then(res => {
          if (res.code === 200) {
            this.okTips('删除成功')
            this.toQuery()
          } else {
            this.failTips(res.message)
          }
        })
      })
    },
    okTips(msg) {
      msg = msg || '成功'
      this.$notify({
        title: msg,
        type: 'success',
        duration: 2500
      })
    },
    failTips(msg) {
      msg = msg || '失败'
      this.$notify({
        title: msg,
        type: 'error',
        duration: 2500
      })
    },
    confirm(msg, params, handler) {
      this.$confirm(msg, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => handler(params)).catch(() => {})
    }
  }
}

const dateTimeOps = {
  shortcuts: [{
    text: '最近一周',
    onClick(picker) {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      picker.$emit('pick', [start, end])
    }
  }, {
    text: '最近一个月',
    onClick(picker) {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      picker.$emit('pick', [start, end])
    }
  }, {
    text: '最近三个月',
    onClick(picker) {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      picker.$emit('pick', [start, end])
    }
  }]
}
