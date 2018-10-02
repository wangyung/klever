function getDslValue() {
    console.info(editor.getValue());
    return editor.getValue()
}

function buildDsl() {
    var clientIdElement = document.getElementById("clientId");
    var payload = {
        code: getDslValue(),
        userId: parseInt(document.getElementById("userId").value),
        clientId: parseInt(clientIdElement.value)
    };
    var logViewer = document.getElementById("logViewer");

    logViewer.innerText = "";
    fetch("/build", {
        method: "post",
        headers: {
            "Accept": "application/json",
            "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
    }).then(function (response) {
        return response.json()
    }).then(function (value) {
        if (value.status === 200) {
            document.getElementById("extensionUrl").textContent = getUrl(value.clientId)
            clientIdElement.value = value.clientId;
            logViewer.innerText = "build clova extension successfully";
        } else {
            logViewer.innerText = value.errorMessage;
        }
    })
}

function getUrl(clientId) {
    var username = document.getElementById("username").value;
    return "https://" + window.location.host + "/" + username + "/" + clientId;
}

function logout() {
    deleteCookie();
    window.location = "/login";
}
