<template>
  <b-card title="Create a new Branch" style="margin:auto;width:fit-content" v-show="showPage">
    <b-form>
      <b-container fluid style="padding-left: 0;">
        <b-row>
          <b-col>
            <b-form-checkbox id="cb1" class="mb-2" v-model="remCurBranch">Replace current branch?</b-form-checkbox>
            <div v-show="!remCurBranch">
              <label>Archive Folder (full path)</label>
              <b-form-input v-model="archFolder" class="border"></b-form-input>
            </div>
          </b-col>
        </b-row>
        <b-row>
          <b-col>
            <label class="sr-only mt-2" for="newBranchName">Name of new branch</label>
            <b-form-input v-model="newBranchName" class="border"></b-form-input>
          </b-col>
        </b-row>
        <b-row>
          <b-col>
            <b-button style="width:100%;" v-on:click="createBranch" class="mt-2" variant="primary">Create Branch</b-button>
          </b-col>
        </b-row>
        <b-overlay :show="loading" variant="light" opacity="0.6" rounded="sm"></b-overlay>
        <b-row v-show="retrieveResult !== ''">
          <b-col class="mt-3">
            <b-alert :variant=errmsg show>{{ retrieveResult }}</b-alert>
          </b-col>
        </b-row>
      </b-container>
    </b-form>
  </b-card>
</template>

<script>
export default {
  name: 'CreateBranch',
  props: ['showPage'],
  data() {
    return {
      remCurBranch: true,
      archFolder: '',
      curBranchName: '',
      newBranchName: '',
      loading: false,
      errmsg: '',
      retrieveResult: ''
    }
  },
  methods: {
    /*
    * Page refresh
    */
    refresh() {
      window.location.reload();
    },
    createBranch() {
      this.loading = true;
      this.retrieveResult = "";
      this.errmsg = "success";
      var url = "/api/createbranch/" + this.newBranchName;
      this.axios.post(url, {branchName: this.curBranchName, archFolder: this.archFolder})
        .then(response => {
          var f = response.data;
          if (String(f).length > 0) {
            this.errmsg = "danger";
            this.retrieveResult = f;
          } else {
            this.retrieveResult = "";
            this.curBranchName = this.newBranchName;
            this.refresh();
          }
        })
        .catch(error => {
          this.retrieveResult = error.message;
          this.errmsg = "danger";
          this.loading = false;
       });
    }
  },
  mounted() {
    this.emitter.on("SetCurBranch", (name) => {
//      console.log("SetCurBranch request: " + name.branchName);
      this.curBranchName = name.branchName;
    });
  }
}
</script>

<style scoped>
</style>
