<template>
	<b-card title="Merge Trunk Flow into Branch" v-show="showPage">
		<b-alert v-show="same" variant="success" show>{{ retrieveResult }}</b-alert>
		<b-tabs v-show="!same && !showMergeCode" card fill>
		<b-tab title="Nodes only in trunk" active>
			<!-- 5-column table: add | tabName | nodeName | nodeContent.type | show_details -->
			<b-table small sticky-header="700px" :items="addedNodes" :fields="nodeAddedFields">
			<template #table-colgroup="">
				<col style="width:5%" center>
				<col style="width:20%">
				<col>
				<col style="width:10%">
				<col style="width:10%">
			</template>
			<template #cell(add)="row">
				<b-form-checkbox id="add" v-model="row.item.add" name="add"></b-form-checkbox>
			</template>
			<template #cell(show_details)="row">
					<b-button size="sm" @click="row.toggleDetails" class="mr-2" v-if="(row.item.curHtml.length > 0)">
					{{ row.detailsShowing ? 'Hide' : 'Show'}} Code
				</b-button>
			</template>
			<template #row-details="row">
					<b-card>
					<b-row class="mb-2">
						<b-col>
					<pre class="code1" v-html="row.item.curHtml"></pre>
				</b-col>
					</b-row>
					</b-card>
			</template>
			</b-table>
		</b-tab>
		<b-tab title="Nodes changed">
			<!-- 5-column table: tabName | nodeName | nodeContent.type | whatChanged | show_details -->
			<b-table small sticky-header="700px" :items="changedNodes" :fields="nodeChangedFields" ref="cn">
			<template #table-colgroup="">
				<col style="width:20%">
				<col>
				<col style="width:10%">
				<col style="width:10%">
				<col style="width:10%">
			</template>
			<template #cell(show_details)="row">
				<b-button size="sm" class="mr-2" :variant="row.item.merged?'success':'primary'" v-on:click="onMergeCode(row.index, row.item)"
				v-if="(row.item.curHtml.length > 0 || row.item.prvHtml.length > 0)">
				Merge Code
				</b-button>
			</template>
			</b-table>
		</b-tab>
		</b-tabs>
	</b-card>
		<!-- Full screen page for code merging -->
		<b-container fluid v-show="!same && showMergeCode && showPage">
			<b-row>
				<!-- when merging function code -->
				<b-col cols="12">
					<b-button size="sm" v-on:click="doneMergingCode" class="mr-2" variant="primary" style="margin-top: 2px;">Done</b-button>
					<b-card>
						<b-row>
							<b-col class="col-code">
								<label style="font-weight: bolder;">Branch</label>
								<b-form-textarea name="syncscroll1" class="syncscroll code1" v-model="currow.curHtml"></b-form-textarea>
							</b-col>
							<b-col class="col-code">
								<label style="font-weight: bolder;">Trunk</label>
								<div name="syncscroll2" class="syncscroll code2" v-html="currow.prvHtml"></div>
							</b-col>
						</b-row>
					</b-card>
				</b-col>
			</b-row>
		</b-container>
</template>

<script>
export default {
	name: 'MergeTrunkFlow',
	props: ['uptodate', 'branchName', 'wcLocation', 'remoteUrl', 'currentBranchRev', 'latestBranchRevs', 'latestTrunkRevs', 'highestTrunkRevInBranch','showPage'],
	data() {
	return {
		loading: false,
		errmsg: '',
		retrieveResult: '',
			changedNodes: [],	 // table of nodes in flows.json that have changed in the trunk
			nodeChangedFields: [
				{key: "tabName", label: "Tab"},
				{key: "nodeName", label: "Node"},
				{key: "nodeContent.type", label: "Type"},
				{key: "whatChanged", label: "what changed?"},
				{key: "show_details", label: "Code"}
			],
			addedNodes: [],		// tabel of nodes in flows.json that appear only in the trunk
			nodeAddedFields: [
				{key: "add", label: "Add?"},
				{key: "tabName", label: "Tab"},
				{key: "nodeName", label: "Node"},
				{key: "nodeContent.type", label: "Type"},
				{key: "show_details", label: "Code"}
			],
		showMergeCode: false,
		same: false,
		currow: {curHtml:'', prvHtml:''}, // .curhtml=branch code, .prvhtml=trunk code
		curix: -1,					// index of selected row in function code table
		curcolor: "primary",	// color of the "Merge"-button
		merged: false,
	}
	},
	methods: {
	getFlowDiff() {
			this.loading = true;
			this.errmsg = "success";
			this.axios.post("/api/changednodes", {
				'branchName':this.branchName,
				'wcLocation':this.wcLocation, 
				'remoteUrl':this.remoteUrl, 
				'currentBranchRev':this.currentBranchRev, 
				'latestBranchRevs':this.latestBranchRevs, 
				'latestTrunkRevs':this.latestTrunkRevs, 
				'highestTrunkRevInBranch':this.highestTrunkRevInBranch })
			.then(response => {
				var f = response.data;
				if (f.added === undefined) {
					this.retrieveResult = "Branch is up to date";
				} else {
					this.addedNodes = f.added;
					this.changedNodes = f.changed;
					if (this.addedNodes.length === 0 && this.changedNodes.length === 0) {
						this.same = true;
						this.retrieveResult = "Flows are the same";
					} else {
						for (var i = 0; i < this.addedNodes.length; i++) {
							this.addedNodes[i].add = true;
						}
						for (var j = 0; j < this.changedNodes.length; j++) {
							this.changedNodes[j].merged = false;
						}
						this.same = false;
						this.retrieveResult = "";
					}
				}
				this.loading = false;
			})
			.catch(error => {
				this.retrieveResult = error.message;
				this.loading = false;
				this.errmsg = "danger";
			});
			this.emitter.emit('MergeUi');
		},
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
			el1.removeEventListener('scroll', el1.syn, 0);
			el2.removeEventListener('scroll', el2.syn, 0);
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
			}, 0);
		},
		/*
		* Handle the "Merge"-button press in the function table
		*/
		onMergeCode(index, row) {
			this.showMergeCode = true;
			this.curix = index;
			this.currow = row;
		},
		/*
		* Handle the "Done"-button press in the Merge page
		*/
		doneMergingCode() {
			this.showMergeCode = false;
			this.changedNodes[this.curix].curHtml = this.currow.curHtml;
			this.changedNodes[this.curix].nodeContent.func = this.currow.curHtml;
			this.changedNodes[this.curix].merged = true;
		},
		areAllMerged() {
			this.merged = true;
			for (var row of this.changedNodes) {
				this.merged = this.merged && row.merged;
			}
			this.emitter.emit('FlowsMerged', this.merged);
		}
	},
	mounted() {
		this.emitter.on('MergeFlow', () => {
//			console.log("MergeFlow request");
			this.getFlowDiff()
		});
		this.emitter.on('AreFlowsMerged', () => {
//			console.log("AreFlowsMerged request");
			this.areAllMerged()
		});
		this.emitter.on('GetChangedNodes', () => {
//			console.log("GetChangedNodes request");
			this.emitter.emit('ChangedNodes', {added: this.addedNodes, changed: this.changedNodes});
		});
		this.showMergeCode = false;
		this.sync();
		this.merged = false;
	}
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style>
.code2 {
	font-family: "Monaco", "Courier New", monospace;
	font-size: smaller;
	height: 600px;
	width: 100%;
	white-space: pre;
	overflow: scroll;
	border-width: 1px;
	border-style: solid;
	line-height: 1.5;
	padding: 3px;
}

.code1 {
	font-family: "Monaco", "Courier New", monospace;
	font-size: smaller;
	height: 600px;
	width: 100%;
	white-space: nowrap;
	overflow: scroll;
}
</style>
