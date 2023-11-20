console.log("this is script file")

const toggleSidebar = () => {
    if($(".sidebar").is(":visible")){
        //true
        //band karana hai

        $(".sidebar").css("display","none");
        $(".content").css("margin-left","0%");
    } else {
        $(".sidebar").css("display","block");
        $(".content").css("margin-left","20%");
    }
};


const search = () => {
    //console.log("searching....")

    let query = $("#search-input").val();

    if(query == ""){
        $(".search-result").hide();
    }else{
        //search
        //console.log(query);

        //sending request to server

        let url = `http://localhost:8080/search/${query}`

        fetch(url).then((response) => {
            return response.json();

        })
        .then((data) => {
            //data.......
            //console.log(data);

            let text = `<div class = 'list-group'>`;

            data.forEach((contact) => {
                text += `<a href='/user/${contact.cId}/contact' class='list-group-item list-group-item-action'> ${contact.name} </a>`
            });

            text += `</div>`;

            $(".search-result").html(text);
            $(".search-result").show();
        })


        
    }

}

/**

const startPayment = () => {
    console.log("payment started...");
    var amount = $("user_amount").val();
    console.log(amount);
    if(amount == "" || amount == null){
        swal("Failed","amount us required !!","error");
        return;
    }

    //code.....
    //we will use ajax too send request to server to create order- jquery
    $.ajax({
        url: "user/create_order",
        data: JSON.stringify({amount: amount}),
        contentType: "application/json",
        type:"POST",
        dataType: "json",
        success: function(response){
            //invoked when success
            console.log(response);
            if(response.status == "created"){
                //open payment form
            }
        },
        error:function(response){
            //invoked when error
            console.log(error)
            alert("something went wrong !!")
        }
    })

}

*/