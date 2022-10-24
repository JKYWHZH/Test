class FileSliceUpload {
    constructor(checkUrl, uploadUrl, margeUrl, fileSelect) {
        this.checkUrl = checkUrl; // 检测文件上传的url
        this.uploadUrl = uploadUrl;//文件上传接口
        this.margeUrl = margeUrl; // 合并文件接口
        this.fileSelect = fileSelect;
        this.fileObj = null;
        this.totalize = null;
        this.blockSize = 10240 * 1024; //每次上传多少字节10mb(最佳)
        this.sta = 0; //起始位置
        this.end = this.sta + this.blockSize; //结束位置
        this.fileName = "";
        this.uploadFileInterval = null;  //上传文件定时器
        this.skip = 0; //当前片数
        this.id = ""; //当前文件唯一标识
    }

    /**
     * 停止上传文件
     */
    stopUploadFile() {
        clearInterval(this.uploadFileInterval)
    }

    /**
     * 开始上传文件
     */
    startUploadFile() {
        this.fileObj = this.fileSelect.files[0];
        this.totalize = this.fileObj.size;
        this.fileName = this.fileObj.name;
        //查询是否存在之前上传过此文件,然后继续
        this.sequelFile();
        let ref = this; //拿到当前对象的引用,因为是在异步中使用this就是他本身而不是class
        console.log("id: "+ ref.id);
        this.uploadFileInterval = setInterval(function () {
            if (ref.sta > ref.totalize) {//如果上传完成，则跳出继续上传
                //上传完毕后结束定时器
                clearInterval(ref.uploadFileInterval)
                ref.margeFile();
                return;
            }
            //分割文件
            let fileData =  ref.fileObj.slice(ref.sta, ref.end);
            let formData = new FormData();
            formData.append("id", ref.id);
            formData.append("file", fileData);//将 部分文件 塞入FormData
            formData.append("skip", ref.skip);
            $.ajax({
                url: ref.uploadUrl,
                type: "POST",
                data: formData,
                processData: false,  // 告诉jQuery不要去处理发送的数据
                contentType: false,   // 告诉jQuery不要去设置Content-Type请求头
                async: false
            }).success(function (data) {
                console.log(data);
                if (data == "success") {
                    //起始位置等于上次上传的结束位置
                    ref.sta =  ref.end;
                    //结束位置等于上次上传的结束位置+每次上传的字节
                    ref.end = ref.sta + ref.blockSize;
                    //更新当前片数
                    ref.skip = ref.skip + 1;
                }
            })
        }, 5)
    }

    /**
     * 合并文件
     */
    margeFile() {
        console.log("准备合并文件")
        var formData = new FormData();//初始化一个FormData对象
        formData.append("id", this.id);
        formData.append("fileName", this.fileName);//保存文件名字
        $.ajax({
            url: this.margeUrl,
            type: "POST",
            data: formData,
            processData: false,  // 告诉jQuery不要去处理发送的数据
            contentType: false,   // 告诉jQuery不要去设置Content-Type请求头
            success: function () {

            }
        });
    }

    /**
     * 续传文件
     */
    sequelFile() {
        console.log("检查是否续传文件")
        let pid = null;
        let formData = new FormData();
        formData.append("id", this.id);
        $.ajax({
            url: this.checkUrl, //上传文件的请求路径必须是绝对路劲
            type: 'post',
            data: formData,
            cache: false,
            processData: false,
            contentType: false,
            async: false
        }).success(function (id) {
            console.log("id:" + id);
            pid = id;
        })
        this.id = pid;
    }
}

export default FileSliceUpload;