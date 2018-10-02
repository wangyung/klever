function checkCookie() {
    var username = getCookie('username');
    if (username === '') {
        // Re-direct to this page
        window.location = '/login';
    } else {
        window.location = '/' + username;
    }
}

function getCookie(cookieName) {
    var name = cookieName + '=';
    decodeURIComponent(document.cookie);
    var keyValues = document.cookie.split(';');
    for(var i=0; i<keyValues.length; i++)
    {
        var cookie = keyValues[i].trim();
        if (cookie.indexOf(name) === 0) return cookie.substring(name.length,cookie.length);
    }
    return '';
}

function setUsername(name) {
    var expireDay = new Date();
    expireDay.setTime(expireDay.getTime() + (7 * 24 * 60 * 60 * 1000));
    document.cookie = 'username=' + name + '; path=/; expires=' + expireDay.toUTCString();
}

function deleteCookie() {
    var date = new Date(); //Create an date object
    date.setTime(date.getTime() - (7 * 24 * 60 * 60 * 1000)); //Set the time to the past. 1000 milliseonds = 1 second
    var expires = "expires=" + date.toUTCString(); //Compose the expirartion date
    document.cookie = "username=;" + expires;
}
