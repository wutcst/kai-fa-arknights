let width = window.innerWidth, height = window.innerHeight;
let squares = [], squareSize = 1920 / (1920 * 0.05), squareLength = squareSize * squareSize;
let crosses = [], crossSize = 1920 / (1920 * 0.05), crossLength = crossSize * crossSize;

const getSquares = () => {
    let length = (height / width).toFixed(1) * squareLength;
    for(let i = 0; i < length; i++){
        squares[i] = {
            id: i
        }
    }
}
const getCrosses = () => {
    let length = (height / width).toFixed(1) * crossLength;
    for(let i = 0; i < length; i++){
        crosses[i] = {
            id: i
        }
    }
}
const init = () => {
    new Vue({
        el: '#wrap',
        data: {
            utils: {
                width: {
                    vw: 100,
                    halfOfVw: 50,
                },
                height: {
                    vh: 100,
                    halfOfVh: 50
                },
                mouse: {
                    x: 0,
                    y: 0
                }
            },
            styles: {
                loading: {
                    div: {width: "0px"},
                    span: {
                        a: {left: "0", display: "block"},
                        b: {right: "0", display: "block"},
                        c: {display: "none"}
                    }
                },
                background: {
                    frame: {opacity: 0.7},
                    image: {backgroundImage: `url('./image/background/1.jpg')`, transform: "scale(1.2)", transition: "transform 1.2s", transitionDelay: "0.3s", transformOrigin: "top"}
                }
            },
            shows: {
                opening: true,
                overlay: false,
                video: false
            },
            arrays: {
                square: squares,
                cross: crosses
            },
            loading: {
                step: 0.75,
                width: 0,
                isPlay: true,
                progress: 0,
                complete: "100%"
            },
            src: ''
        },
        mounted(){
            this.animate();
            window.addEventListener('resize', this.onWindowResize, false);
        },
        methods: {
            changeBG(isImage, id){
                if(isImage){
                    this.shows.video = false;
                    this.$refs.videobg.currentTime = 0;
                    this.$refs.videobg.pause();
                    switch (id){
                        case 0:
                            this.styles.background.image.backgroundImage = `url(./image/background/1.jpg)`;
                            this.styles.background.frame.opacity = '0.7';
                            break;
                        case 1:
                            this.styles.background.image.backgroundImage = `url(./image/background/2.jpg)`;
                            this.styles.background.frame.opacity = '0.85';
                            break;
                        case 2:
                            this.styles.background.image.backgroundImage = `url(./image/background/6.jpg)`;
                            this.styles.background.frame.opacity = '0.75';
                            break;
                        case 3:
                            this.styles.background.image.backgroundImage = `url(./image/background/7.jpg)`;
                            this.styles.background.frame.opacity = '0.5';
                            break;
                        case 4:
                            this.styles.background.image.backgroundImage = `url(./image/background/8.jpg)`;
                            this.styles.background.frame.opacity = '0.5';
                            break;
                    }
                }else{
                    this.shows.video = true;
                    this.$refs.videobg.currentTime = 0;
                    this.styles.background.frame.opacity = '0.7';
                    switch (id){
                        case 0:
                            this.$refs.videobg.src = './video/1.webm';
                            break;
                        case 1:
                            this.$refs.videobg.src = './video/2.webm';
                            break;
                        case 2:
                            this.$refs.videobg.src = './video/3.webm';
                            break;
                    }
                    this.$refs.videobg.play();
                }
            },
            displayOverlay(){
                this.shows.overlay = true;
            },
            hideOverlay(){
                this.shows.overlay = false;
            },
            mouseMove: throttle(function(e) {
                this.utils.mouse.x = e.clientX;
                this.utils.mouse.y = e.clientY;
            }, 50),
            resizeSquareLength(){
                let length = (height / width).toFixed(1) * squareLength;
                this.arrays.square = [];
                squares = [];
                for(let i = 0; i < length; i++){
                    squares[i] = {
                        id: i, 
                        style: {boxShadow: "0px 0px 20px rgba(0, 0, 0, 0)", opacity: "0", transition: "box-shadow, opacity 0.3s"}
                    }
                }
                this.arrays.square = squares;
            },
            resizeCrossLength(){
                let length = (height / width).toFixed(1) * crossLength;
                this.arrays.cross = [];
                crosses = [];
                for(let i = 0; i < length; i++){
                    crosses[i] = {
                        id: i
                    }
                }
                this.arrays.cross = crosses;
            },
            onWindowResize(){
                width = window.innerWidth;
                height = window.innerHeight;
                this.resizeSquareLength();
                this.resizeCrossLength();
            },
            loadCompleted(){
                this.styles.loading.span.a.display = "none";
                this.styles.loading.span.b.display = "none";
                this.styles.loading.span.c.display = "block";
                this.loading.isPlay = false;
                this.shows.opening = false;
                this.styles.background.image.transform = "scale(1.0)";
            },
            loadingBar(){
                if(this.loading.width < this.utils.width.halfOfVw){
                    this.loading.width += this.loading.step;
                    this.styles.loading.div.width = `${this.loading.width}vw`;
                    this.styles.loading.span.a.left = `${this.loading.width - this.loading.step * 2}vw`;
                    this.styles.loading.span.b.right = `${this.loading.width - this.loading.step * 2}vw`;
                    this.loading.progress = Math.floor(this.loading.width / this.utils.width.halfOfVw * 100);
                }
                else {
                    this.loadCompleted();
                }
            },
            render(){
                if(this.loading.isPlay) this.loadingBar();
            },
            animate(){
                this.render();
                requestAnimationFrame(this.animate);
            }
        }
    })
}
const render = () => {
    getSquares();
    getCrosses();
    init();
}
render();