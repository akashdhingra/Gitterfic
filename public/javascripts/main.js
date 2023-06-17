var searchResults = {};
var searchKeys = [];
var repositories;
var res;
Spinner();
Spinner.hide();
var input = document.getElementById("searchTerm");
input.addEventListener("keyup", function(event) {
    if (event.keyCode === 13) {
        search();
    }
});

function searchDisplay(resultData, searchkey) {
		var result = JSON.parse(resultData);
		var resultDiv = document.getElementById("searchResultDiv");
		repositories = result.data.repositories;
		var	mainDiv = document.getElementById("kw_"+searchkey);
		if (result.data.isNewData) {
			mainDiv = document.createElement("div");
			mainDiv.id = "kw_"+searchkey;
			resultDiv.insertBefore(mainDiv, resultDiv.firstChild);
			searchKeys.push(searchkey);
			if (searchKeys.length > 10) {
			  var key = searchKeys.shift();
			  document.getElementById("kw_"+key).remove();
			}
		}
		else {
		}
    var count = 1;
    var txt = "<h4>Search Term: " + searchkey + "</h4>";
    var result = JSON.parse(resultData);
    repositories = result.data.repositories;
    console.log(result);
    txt += "<table class='table table-bordered'><tr><th>#</th><th>Owner</th><th>Repository Name</th><th>Topics</th></tr>";
    if (repositories.length == 0) {
        txt += "<tr><td colspan='4'>No search results for the term</td></tr>"
    } else {
      for (x in repositories) {
        txt += "<tr><td>" + count + ".</td><td><a href='http://localhost:9000/users/" + repositories[x].owner.login + "'>" + repositories[x].owner.login + "</a></td><td><a href='http://localhost:9000/repos/" + repositories[x].owner.login + "/" + repositories[x].name + "'>" + repositories[x].name + "</a></td><td>" + repositories[x].topics + "</td></tr>";
        count++;
      }
    }
    txt += "</table><br/>";
		searchResults[searchkey] = txt;
    mainDiv.innerHTML = txt;
    document.getElementById("searchTerm").value = "";
    Spinner.hide();
}

//Web Socket implemnetation for searching keyword
function search() {
  var searchkey = document.getElementById("searchTerm").value;
  Spinner.show();

  let message = {
    "keyword": searchkey
  };
  let msg = JSON.stringify(message);
  let searchSocket = new WebSocket("ws://localhost:9000/getRepositoriesBySearchViaWebSocket");
  searchSocket.onopen = () => searchSocket.send(msg);

  searchSocket.onmessage = function(event) {
      var response = event.data;
      searchDisplay(response, searchkey);
  }

  // searchSocket.onclose = function(event) {
  //   if (event.wasClean) {
  //     alert(`[close] Connection closed cleanly, code=${event.code} reason=${event.reason}`);
  //   } else {
  //     alert('[close] Connection died');
  //   }
  // };

  // searchSocket.onerror = function(error) {
  //   Spinner.hide();
  //   // alert(`[error] ${error.message}`);
  // };
}

function repository(owner, repo) {
	let message = {
	  "owner": owner,
	  "repo": repo
	};
	let msg = JSON.stringify(message);
	let searchSocket = new WebSocket("ws://localhost:9000/getRepositoryDetailViaWebSocket");
	searchSocket.onopen = () => searchSocket.send(msg);

	searchSocket.onmessage = function(event) {
    var response = event.data;
    console.log(response);
    repositoryDisplay(response);
	}
}

function repositoryDisplay(resultData) {
	var result = JSON.parse(resultData);
	var repository = result.data.repository;
	document.getElementById("repoName").innerHTML = repository.name;
	document.getElementById("userName").innerHTML = "<a href='http://localhost:9000/users/"+repository.owner.login+"'>"+repository.owner.login;
	document.getElementById("issueLink").innerHTML = "<a href='http://localhost:9000/repos/"+repository.owner.login+"/"+repository.name+"/issues'>Issues</a>";
	document.getElementById("commitLink").innerHTML = "<a href='http://localhost:9000/repos/"+repository.owner.login+"/"+repository.name+"/commits'>Commits</a>";
	var tbody = document.getElementById("issueTable");
	tbody.innerHTML = "";
	var tr,td;
	result.data.issues.forEach((issue) => {
		tr = document.createElement("tr");
		tbody.appendChild(tr);
		tr.style.fontSize = "1.1rem";
		td = document.createElement("td");
		tr.appendChild(td);
		td.innerHTML = "<a href='http://localhost:9000/users/"+issue.user.login+"'>"+issue.user.login+"</a>";
		td = document.createElement("td");
		tr.appendChild(td);
		td.innerHTML = issue.title;
	});
}

function issue(owner,repo){
    let message = {
      "owner": owner,
      "repo": repo
    };
    let msg = JSON.stringify(message);
    console.log(msg);
    let issueSocket = new WebSocket("ws://localhost:9000/getIssueDetailViaWebSocket");
    issueSocket.onopen = () => issueSocket.send(msg);

    issueSocket.onmessage = function(event) {
        var response = event.data;
        console.log(response);
        issueDisplay(response);
    }
}

function issueDisplay(resultData){
    var result = JSON.parse(resultData);
    var issues = result.data.issues;
    var stats = result.data.stats;
    console.log(stats);
    var mainDiv = document.getElementById("issueDiv");
    mainDiv.innerHTML = "";
    var innerDiv,h5;
    issues.forEach((issue) => {
        innerDiv = document.createElement("div");
        mainDiv.appendChild(innerDiv);
        h5 = document.createElement("h5");
        innerDiv.appendChild(h5);
        h5.innerHTML = issue.title;
    });

    mainDiv=document.getElementById("wordStatsDiv");
    mainDiv.innerHTML = "";
    for (let i of Object.keys(stats.wordfrequency)){
        var stat = stats.wordfrequency[i];
        innerDiv = document.createElement("div");
        mainDiv.appendChild(innerDiv);
        h5 = document.createElement("h5");
        innerDiv.appendChild(h5);
        h5.innerHTML = i+" : "+stat;
    }

}

function user(userName) {
	let message = {
	  "user": userName
	};
	console.log(message);
	let msg = JSON.stringify(message);
	let searchSocket = new WebSocket("ws://localhost:9000/getUserDetailViaWebSocket");
	searchSocket.onopen = () => searchSocket.send(msg);

	searchSocket.onmessage = function(event) {
    var response = event.data;
    console.log(response);
    userDisplay(response);
	}
}

function userDisplay(resultData) {
	var result = JSON.parse(resultData);
	var user = result.data.user;
	document.getElementById("userName").innerHTML = user.login;
	document.getElementById("followers").innerHTML = user.followers;
	document.getElementById("following").innerHTML = user.following;
	document.getElementById("userEmail").innerHTML = user.email;
	document.getElementById("location").innerHTML = user.location;

	var mainDiv = document.getElementById("userDiv");
	mainDiv.innerHTML = "";
	var innerDiv, h5;
	result.data.repositories.forEach((repository) => {
		innerDiv = document.createElement("div");
		mainDiv.appendChild(innerDiv);
		h5 = document.createElement("h5");
		innerDiv.appendChild(h5);
		h5.innerHTML = "<a href='http://localhost:9000/repos/"+user.login+"/"+repository.name+"'>"+repository.name+"</a>";
	});
}

function commits(owner, repo) {
	let message = {
	  "owner": owner,
	  "repo": repo
	};
	console.log(message);
	let msg = JSON.stringify(message);
	let commitSocket = new WebSocket("ws://localhost:9000/getCommitDetailViaWebSocket");
	commitSocket.onopen = () => commitSocket.send(msg);

	commitSocket.onmessage = function(event) {
    var response = event.data;
    commitDisplay(response);
	}
}

function commitDisplay(resultData) {
	var result = JSON.parse(resultData);
	res = result;
  console.log(res);
	var mainDiv = document.getElementById("commitDiv");
	mainDiv.innerHTML = "";
	var innerDiv, div, h3, h4, h5, ol, li;
	if (result.data.commits.length == 0) {
		innerDiv = document.createElement("div");
		mainDiv.appendChild(innerDiv);
		innerDiv.className = "col-sm-12";
		h3 = document.createElement("h3");
		mainDiv.appendChild(h3);
		h3.innerHTML = "No commits found in this repository";
	}
	else {
		innerDiv = document.createElement("div");
		mainDiv.appendChild(innerDiv);
		innerDiv.className = "col-sm-12";
		h4 = document.createElement("h4");
		innerDiv.appendChild(h4);
		h4.innerHTML = "Commit Frequency:";
		ol = document.createElement("ol");
		innerDiv.appendChild(ol);
		for (let i of Object.keys(result.data.count_stats.frequency)) {
			var stat = result.data.count_stats.frequency[i];
			li = document.createElement("li");
			ol.appendChild(li);
			li.style.fontSize = "1.25rem";
			h5 = document.createElement("h5");
			li.appendChild(h5);
			h5.innerHTML = "<a href='http://localhost:9000/users/"+i+"'>"+i+"</a> : "+stat;
		}

		innerDiv = document.createElement("div");
		mainDiv.appendChild(innerDiv);
		innerDiv.className = "col-sm-12";
		
		h5 = document.createElement("h5");
		innerDiv.appendChild(h5);
		h5.innerHTML = "Maximum Additions: "+result.data.addition_stats.mod_stats.max;
		h5 = document.createElement("h5");
		innerDiv.appendChild(h5);
		h5.innerHTML = "Minimum Additions: "+result.data.addition_stats.mod_stats.min;
		h5 = document.createElement("h5");
		innerDiv.appendChild(h5);
		h5.innerHTML = "Average Additions: "+result.data.addition_stats.mod_stats.avg;
		
		h5 = document.createElement("h5");
		innerDiv.appendChild(h5);
		h5.innerHTML = "Maximum Deletions: "+result.data.deletion_stats.mod_stats.max;
		h5 = document.createElement("h5");
		innerDiv.appendChild(h5);
		h5.innerHTML = "Minimum Deletions: "+result.data.deletion_stats.mod_stats.min;
		h5 = document.createElement("h5");
		innerDiv.appendChild(h5);
		h5.innerHTML = "Average Deletions: "+result.data.deletion_stats.mod_stats.avg;
		
		innerDiv = document.createElement("div");
		mainDiv.appendChild(innerDiv);
		innerDiv.className = "col-sm-12";
		ol = document.createElement("ol");
		innerDiv.appendChild(ol);
		result.data.commits.forEach((commit) => {
			li = document.createElement("li");
			ol.appendChild(li);
			li.style.fontSize = "1.75rem";
			h4 = document.createElement("h4");
			li.appendChild(h4);
			h4.innerHTML = commit.message;
			h5 = document.createElement("h5");
			li.appendChild(h5);
			var txt = '';
			if (commit.author == null) {
				txt += "<i>Deleted User</i>";
			}
			else {
				txt += "<a href='http://localhost:9000/users/'"+commit.author.login+"'>"+commit.author.login+"</a>";
			}
			txt += "(+"+commit.additions+", -"+commit.deletions+")";
			h5.innerHTML = txt;
		});
	}
}
