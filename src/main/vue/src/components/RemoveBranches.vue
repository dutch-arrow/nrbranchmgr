<template>
  <b-card title="Remove my Branches" style="margin:auto;width:fit-content" v-show="showPage">
    <b-form>
      <b-container fluid style="padding-left: 0;">
        <b-row>
          <b-col cols="8">
            <b-form-select style="width:100%;" v-model="selectedBranches" :options="myBranches" multiple></b-form-select>
          </b-col>
          <b-col>
            <b-button :disabled="myBranches.length === 1" style="width:100%;" v-on:click="removeBranches" class="mr-0" variant="primary" fill>Remove</b-button>
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
  name: 'RemoveBranches',
  props: ['branchName', 'showPage'],
  data() {
    return {
      myBranches: [],
      selectedBranches: '',
      loading: false,
      errmsg: '',
      retrieveResult: ''
    }
  },
  methods: {
    getMyBranches() {
      this.loading = true;
      this.retrieveResult = "";
      this.errmsg = "success";
      this.myBranches = [];
      this.axios.post("/api/getbranches")
      .then(response => {
        for (var i = 0; i < response.data.length; i++) {
          if (response.data[i] === this.branchName) {
            this.myBranches.push({text:response.data[i],disabled:true});
          } else {
            this.myBranches.push(response.data[i]);
          }
        }
        this.selectedBranches = [];
        this.loading = false;
      })
      .catch(error => {
        console.log(error.message);
        this.retrieveResult = error.message;
        this.errmsg = "danger";
        this.loading = false;
      });
    },
    removeBranches() {
      this.loading = true;
      this.retrieveResult = "";
      this.errmsg = "success";
      this.axios.post("/api/removebranches", this.selectedBranches)
        .then(response => {
          var f = response.data;
          if (String(f).startsWith("svn")) {
            console.log(f);
            this.retrieveResult = f;
          } else {
            this.retrieveResult = "";
            this.getMyBranches();
          }
          this.loading = false;
        })
        .catch(error => {
          console.log(error.message);
          this.retrieveResult = error.message;
          this.errmsg = "danger";
          this.loading = false;
        });
    }
  },
  mounted() {
    this.emitter.on('RemoveBranches', () => {this.getMyBranches()});
   }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
