use std::thread;

thread::spawn(move || {
    println!("this is a message");
    println!("this is another message");
});
<caret>