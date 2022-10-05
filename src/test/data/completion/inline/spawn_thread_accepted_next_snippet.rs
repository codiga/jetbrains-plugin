use std::thread;

thread::spawn(move || {
    println!("this is a message");
});
<caret>