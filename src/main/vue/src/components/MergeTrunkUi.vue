<template>
	<b-card title="Merge Trunk UI into Branch" v-show="showPage">
		<b-tabs card fill>
			<b-tab title="HTML" active>
				<b-alert v-show="showMsg[0]" :variant="errmsg[0]" show>{{ retrieveResult[0] }}</b-alert>
				<b-container fluid v-show="!showMsg[0]">
					<b-row>
						<!-- when merging UI (html, js and css) -->
						<b-col cols="12">
							<b-button size="sm" v-on:click="doneMergingUi('html')" class="mr-2" variant="primary" style="margin-top: 2px;">Done</b-button>
							<b-card>
								<b-row>
									<b-col class="col-code">
										<label style="font-weight: bolder;">Branch</label>
										<b-form-textarea name="syncscroll1" class="syncscroll code1" v-model="ui.html[0]"></b-form-textarea>
									</b-col>
									<b-col class="col-code">
										<label style="font-weight: bolder;">Trunk</label>
										<div name="syncscroll2" class="syncscroll code2" v-html="ui.html[1]"></div>
									</b-col>
								</b-row>
							</b-card>
						</b-col>
					</b-row>
				</b-container>
			</b-tab>
			<b-tab title="Javascript">
				<b-alert v-show="showMsg[1]" :variant="errmsg[1]" show>{{ retrieveResult[1] }}</b-alert>
				<b-container fluid v-show="!showMsg[1]">
					<b-row>
						<b-col cols="12">
							<b-button size="sm" v-on:click="doneMergingUi('js')" class="mr-2" variant="primary" style="margin-top: 2px;">Done</b-button>
							<b-card>
								<b-row>
									<b-col class="col-code">
										<label style="font-weight: bolder;">Branch</label>
										<b-form-textarea name="syncscroll3" class="syncscroll code1" v-model="ui.js[0]"></b-form-textarea>
									</b-col>
									<b-col class="col-code">
										<label style="font-weight: bolder;">Trunk</label>
										<div name="syncscroll4" class="syncscroll code2" v-html="ui.js[1]"></div>
									</b-col>
								</b-row>
							</b-card>
						</b-col>
					</b-row>
				</b-container>
			</b-tab>
			<b-tab title="CSS">
				<b-alert v-show="showMsg[2]" :variant="errmsg[2]" show>{{ retrieveResult[2] }}</b-alert>
				<b-container fluid v-show="!showMsg[2]">
					<b-row>
						<b-col cols="12">
							<b-button size="sm" v-on:click="doneMergingUi('css')" class="mr-2" variant="primary" style="margin-top: 2px;">Done</b-button>
							<b-card>
								<b-row>
									<b-col class="col-code">
										<label style="font-weight: bolder;">Branch</label>
										<b-form-textarea name="syncscroll5" class="syncscroll code1" v-model="ui.css[0]"></b-form-textarea>
									</b-col>
									<b-col class="col-code">
										<label style="font-weight: bolder;">Trunk</label>
										<div name="syncscroll6" class="syncscroll code2" v-html="ui.css[1]"></div>
									</b-col>
								</b-row>
							</b-card>
						</b-col>
					</b-row>
				</b-container>
			</b-tab>
		</b-tabs>
	</b-card>
</template>

<script>
export default {
	name: 'MergeTrunkUi',
	props: ['showPage'],
	data() {
		return {
			showMsg: [false,false,false],
			retrieveResult: ['','',''],
			errmsg: ['success','success','success'],
			loading: false,
			ui: {html:[], js:[], css:[]},
			merged: [false,false,false],
      supportsPassive: false,
      same: false,
      branchInfo: {
        branchName: '', 
        wcLocation: '', 
        remoteUrl: '', 
        currentBranchRev: 0, 
        latestBranchRevs:[0,0,0,0], 
        latestTrunkRevs:[0,0,0,0],
        highestTrunkRevInBranch: 0,
      },
		}
	},
	methods: {
		/*
		* Handle the scroll synchronization of the merge page
		*/
		sync() {
			this.$nextTick(function () {
				this.reset();
			});
		},
		reset() {
			var elems = document.getElementsByClassName("syncscroll");
			var el1 = elems[0];
			var el2 = elems[1];
			// clearing existing listeners
			el1.removeEventListener('scroll', el1.syn, this.supportsPassive ? { passive: true } : false);
			el2.removeEventListener('scroll', el2.syn, this.supportsPassive ? { passive: true } : false);
			// setting-up the new listeners
			el1.eX = el1.eY = el2.eX = el2.eY = 0;
			this.syncScroll(el1, el2);
			this.syncScroll(el2, el1);
		},
		syncScroll(el, otherEl) {
			el.addEventListener('scroll', el.syn = function () {
				var scrollX = el.scrollLeft;
				var scrollY = el.scrollTop;
				var xRate = scrollX / (el.scrollWidth - el.clientWidth);
				var yRate = scrollY / (el.scrollHeight - el.clientHeight);
				var updateX = scrollX != el.eX;
				var updateY = scrollY != el.eY;
				el.eX = scrollX;
				el.eY = scrollY;
				if (updateX && Math.round(otherEl.scrollLeft -
					(scrollX = otherEl.eX = Math.round(xRate * (otherEl.scrollWidth - otherEl.clientWidth))))
				) {otherEl.scrollLeft = scrollX;}
				if (updateY && Math.round(otherEl.scrollTop -
					(scrollY = otherEl.eY = Math.round(yRate * (otherEl.scrollHeight - otherEl.clientHeight))))
				) {otherEl.scrollTop = scrollY;}
			}, this.supportsPassive ? { passive: true } : false);
		},
		/*
		* Retrieve the changed data of the UI files
		*/
		getUiDiff() {
			this.errmsg = ['success','success','success'];
			this.ui = {html:[], js:[], css:[]};
			this.html = "";
			this.js = "";
			this.css = "";
			if (this.branchInfo.latestBranchRevs[1] == 0 && this.branchInfo.latestBranchRevs[2] == 0 && this.branchInfo.latestBranchRevs[3] == 0 &&
				this.branchInfo.latestTrunkRevs[1] == 0 && this.branchInfo.latestTrunkRevs[2] == 0 && this.branchInfo.latestTrunkRevs[3] == 0) {
				// No UI present
				this.showMsg = [true, true, true];
				this.merged = [true, true, true];
				this.retrieveResult = ["No UI present", "No UI present", "No UI present"];
        this.same = true;
        console.log("No UI present");
        this.emitter.emit("UiIsTheSame", true);
			} else {
				this.loading = true;
				this.axios.post("/changeduis", {
				'branchName':this.branchInfo.branchName,
				'wcLocation':this.branchInfo.wcLocation, 
				'remoteUrl':this.branchInfo.remoteUrl, 
				'currentBranchRev':this.branchInfo.currentBranchRev, 
				'latestBranchRevs':this.branchInfo.latestBranchRevs, 
				'latestTrunkRevs':this.branchInfo.latestTrunkRevs, 
				'highestTrunkRevInBranch':this.branchInfo.highestTrunkRevInBranch })
				.then(response => {
					var f = response.data;
          this.same = true;
					if (f.html === "") {
						this.showMsg[0] = true;
						this.retrieveResult[0] = "htmls are the same";
						this.merged[0] = true;
            this.same &= true;
					} else {
						this.ui.html = f.html;
					}
					if (f.js === "") {
						this.showMsg[1] = true;
						this.retrieveResult[1] = "javascripts are the same";
						this.merged[1] = true;
            this.same &= true;
					} else {
						this.ui.js = f.js;
					}
					if (f.css === "") {
						this.showMgs[2] = true;
						this.retrieveResult[2] = "csses are the same";
						this.merged[2] = true;
            this.same &= true;
					} else {
						this.ui.css = f.css;
					}
					this.loading = false;
          this.emitter.emit("UiIsTheSame", this.same);
				})
				.catch(error => {
//					console.log(error.message);
					this.errmsg = ['danger','danger','danger'];
					this.showMsg = [true, true, true];
					this.retrieveResult[0] = error.message;
					this.retrieveResult[1] = error.message;
					this.retrieveResult[2] = error.message;
				});
			}
		},
		/*
		* Handle the "Done"-button press in the Merge page of UI
		*/
		doneMergingUi(type) {
			this.merging = false;
			this.showMergeUi = false;
			if (type === "html") {
				this.html = this.ui.html[0];
				this.merged[1] = true;
			} else if (type === "js") {
				this.js = this.ui.js[0];
				this.merged[2] = true;
			} else {
				this.css = this.ui.css[0];
				this.merged[3] = true;
			}
		},
	},
	mounted() {
    // Test via a getter in the options object to see if the passive property is accessed
    this.supportsPassive = false;
    try {
      var opts = Object.defineProperty({}, 'passive', {
        get: function() {
          this.supportsPassive = true;
          return true;
        }
      });
      window.addEventListener("testPassive", null, opts);
      window.removeEventListener("testPassive", null, opts);
    } catch (e) { console.log(e); }
		this.emitter.on('MergeUi', (bi) => {
			console.log("MergeUi request:", bi);
      this.branchInfo = bi;
			this.getUiDiff();
		});
    this.emitter.on('IsUiTheSame', () => {
      console.log("IsUiTheSame request");
      this.emitter.emit("UiIsTheSame", {same: this.same});
    });
		this.emitter.on('IsUiMerged', () => {
			console.log("IsUiMerged request");
			var allMerged = this.merged[0] && this.merged[1] && this.merged[2];
			this.emitter.emit('UiMerged', allMerged);
		});
		this.emitter.on('GetChangedHtml', () => {
			console.log("GetChangedHtml request");
			this.emitter.emit('ChangedHtml', this.html);
		});
		this.emitter.on('GetChangedJs', () => {
			console.log("GetChangedJs request");
			this.emitter.emit('ChangedJs', this.js);
		});
		this.emitter.on('GetChangedCss', () => {
			console.log("GetChangedCss request");
			this.emitter.emit('ChangedCss', this.css);
		});
	}
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
