'use strict';

const e = React.createElement;

class LikeButton extends React.Component {
  constructor(props) {
    super(props);
    this.state = { liked: false };
  }

  render() {
    if (this.state.liked) {
		const usersAction = async () => {
	    	var content = document.getElementById('like_button_container');
	  		
	  		// 'https://jsonplaceholder.typicode.com/users/1'
	  		// 'https://jsonplaceholder.typicode.com/posts?_limit=10'
	  		// 'https://my-json-server.typicode.com/typicode/demo/db'
	  		// 'http://127.0.0.1:7000/listUsersNames'
	  		const response = await fetch('http://127.0.0.1:7000/listUsersNames', {
	  		//const response = await fetch('https://my-json-server.typicode.com/typicode/demo/db', {
	   			method:'GET', //'POST',
	    		// body:{}, // myBody, // string or object - only for POST
	    		// mode:'no-cors', // for opaque
	    		headers: {
	      			'Content-Type':'application/json'
	    		}
	  		});
	  
	  		
	  		const myJson = await response.json(); //extract JSON from the http response
		  	console.log(myJson);
		  	
		  	content.innerHTML +=  '<br />' + JSON.stringify(myJson) + '<br />';
		  	//return JSON.stringify(myJson).toString();
		  		
		}
		
		usersAction();
		
    }

    return e(
      'button',
      { onClick: () => this.setState({ liked: true }) },
      'Like'
    );
  }
}

const domContainer = document.querySelector('#like_button_container');
ReactDOM.render(e(LikeButton), domContainer);
