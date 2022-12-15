<template>
  <b-navbar type="dark" variant="primary">
    <b-dropdown id="dropdown-1" text="Menu" variant="primary">
      <b-dropdown-item-button v-on:click="showThePage('home')">Home</b-dropdown-item-button>
      <b-dropdown-item-button v-on:click="showThePage('newBranch')">Create a new Branch</b-dropdown-item-button>
      <b-dropdown-item-button v-on:click="showThePage('remBranch')">Remove Branches</b-dropdown-item-button>
    </b-dropdown>
    <div class="navbar-menu-title">Web UI</div>
    <b-button v-on:click="resetCredentials" class="mr-2" variant="primary" style="float:right">Reset credentials</b-button>
   </b-navbar>
    <login v-show="showLogin" @hideLogin="showLogin=false"></login>
    <home :showPage="activePage === 'home' && !showLogin"></home>
    <create-branch :showPage="activePage === 'newBranch' && !showLogin"></create-branch>
    <remove-branches :branchName="branchName" :showPage="activePage === 'remBranch' && !showLogin"></remove-branches>
</template>

<script>
import Login from './components/Login.vue'
import Home from './components/Home.vue'
import CreateBranch from './components/CreateBranch.vue'
import RemoveBranches from './components/RemoveBranches.vue'

export default {
  name: 'BranchMgr',
  components: {
    Home,
    Login,
    CreateBranch,
    RemoveBranches
  },
  data() {
    return {
      errorMsgs:"",
      showLogin: false,
      // Login dialog data
      uname: "",
      nameState: null,
      pwd: "",
      pwdState: null,
      activePage: "",
      uptodate: false,
      branchName: '',
    }
  },
  methods: {
    /*
    * Retrieve a cookie with the give name
    */
    getCookie(cname) {
      let name = cname + "=";
      let decodedCookie = decodeURIComponent(document.cookie);
      let ca = decodedCookie.split(';');
      for(let i = 0; i <ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) == ' ') { c = c.substring(1); }
        if (c.indexOf(name) == 0) {  return c.substring(name.length, c.length); }
      }
      return "";
    },
    /*
    * Check if there are cookies stored. If not, show dialog
    */
    checkCookies() {
      this.uname = this.getCookie("SvnUser");
      this.pwd = this.getCookie("SvnPwd");
      if (this.uname === "" || this.pwd === "") {
        this.showLogin = true;
      }
    },
    showThePage(page) {
      this.activePage = page;
      if (page === 'remBranch') this.emitter.emit('RemoveBranches');
    },
    /*
    * Handle the "Reset credentials"-button press
    */
    resetCredentials() {
//      console.log("Reset credentials");
      this.showLogin = true;
    },
  },
  // Lifecycle hooks are called at different stages of a component's lifecycle.
  // This function will be called when the component is mounted.
  mounted() {
    this.emitter.on("SetCurBranch", (name) => {
//      console.log("SetCurBranch request: " + name.branchName);
      this.branchName = name.branchName;
    });
    this.checkCookies();
    this.showThePage("home");
  }
}
</script>

<style>
.navbar-menu-title {
  margin: auto;
  color: white !important;
  font-size: 24px;
}
label {
  color:darkblue;
  font-weight: bolder;
}
.card-title {
  color:darkblue;
  text-align: center;
}
</style>
