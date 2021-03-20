let stompClient = null

function createUser() {
    const user = {
        "login": $("#login").val(),
        "password": $("#password").val(),
        "role": $("#role").checked ? "admin" : "user"
    }
    console.log(`sending request to create user = ${user}`)
    stompClient.send("/app/create", {}, JSON.stringify(user))
}

function getAllUsers() {
    stompClient.send("/app/users", {}, "")
}

function subscribeOnCreateResponse() {
    stompClient.subscribe(
        "/user/queue/reply/create",
        message => {
            console.log(`Response message: ${message}`)
            $("#creationStatus").text(JSON.stringify(message.body).replaceAll("\\", ""))
        })
}

function subscribeOnGetAllUsersReply() {
    stompClient.subscribe(
        "/user/queue/reply/users",
        message => {
            console.log(`Response message: ${message}`)
            const tableBody = $("table.users > tbody")
            tableBody.empty()
            const users = JSON.parse(message.body)
            for (let i = 0; i < users.length; i++) {
                const user = users[i]
                tableBody.append(
                    `<tr>
                    <td>${user.login}</td>
                    <td>${user.password}</td>
                    <td>${user.role}</td>
                    </tr>`
                )
            }
        }
    )
}

$(
    () => {
        console.log("Connecting...")
        stompClient = Stomp.over(new SockJS('/messages'))
        stompClient.connect({}, (frame) => {
            console.log(`Connected: ${frame}`)
            subscribeOnCreateResponse()
            subscribeOnGetAllUsersReply()
        })
        $("#createUser").click(createUser)
        $("#getAllUsers").click(getAllUsers)
    }
)