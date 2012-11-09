//groovy script
import java.io.*
import java.util.*
import java.text.SimpleDateFormat
import org.apache.log4j.*
import java.util.concurrent.*

log = Logger.getLogger(this.class);

def startTime=System.currentTimeMillis()
def backedup=0

// allocate a worker pool with 2 threads. To allocate more threads (and parallelize more)
// change the argument value. Setting a value larger than 4 might hurt performance,
// optimal values are 2 or 3 (YMMV).
def pool = Executors.newFixedThreadPool(2)
def tasks=[]

def now=new Date()

def backupOperation = { server, user, password, vm, vmObj ->
	if (vmObj.needsBackup(now)) {
		backedup++
		log.info "backing up ${vm} into "+vmObj.getNextBackupPath(now)

		def out = new StringBuilder()
		def err = new StringBuilder()
		
		def proc = ("C:\\Program Files\\VMware\\VMware Consolidated Backup Framework\\vcbMounter.exe -h ${server} -u ${user} -p ${password} -t fullvm -r "+vmObj.getNextBackupPath(now)+" -a name:${vm} -m san -M 1 -F 1").execute()
		proc.waitForProcessOutput(out, err)
		vmObj.saveLogFile(out,err)
		if (proc.exitValue()!=0) {
			log.error "[${vm} backup failed] vcbMounter exit code: "+proc.exitValue()
		}
		
		vmObj.purgeOldBackups()
	} else {
		log.info "${vm} does not need to be backed up"
	}
}

try {
	def config=new Properties()
	config.load(new FileInputStream("vcb.config"))

	config.get("vcb.servers").split(",").each { server ->
		log.info "Processing server: ${server}"
		def user=config.get("vcb.server.${server}.user".toString())
		def password=config.get("vcb.server.${server}.password".toString())
		
		if(config.get("vcb.server.${server}.vms".toString())) {
			config.get("vcb.server.${server}.vms".toString()).split(",").each { v ->
				// remove white spaces
				def vm=v.trim()
				def vmObj=new VM(name:vm, server:server, config:config, log:log).init()
				log.info vmObj
				
				tasks << pool.submit( { -> backupOperation server, user, password, vm, vmObj } as Callable )
			}
		}
	}
	
	tasks.collect { it.get() }	
} catch (Exception e) {
	log.error("backup failed  because of an exception: "+e.getMessage())
	log.error(e)
}

class VM {
	def df=new SimpleDateFormat("ddMMyyyy")
	def name
	def interval
	def protection
	def server
	def config
	def log
	
	def init() {
		protection = config.get("vcb.server.${server}.vm.${name}.protection".toString())!=null ?
			config.get("vcb.server.${server}.vm.${name}.protection".toString()) : config.get("vcb.server.${server}.protection".toString()) 
		interval = config.get("vcb.server.${server}.vm.${name}.interval".toString())!=null ?
			config.get("vcb.server.${server}.vm.${name}.interval".toString()) : config.get("vcb.server.${server}.interval".toString()) 
		return this
	}
	
	public String toString() {
		return "vm: ${name} [protection=${protection},interval=${interval}]"
	}
	
	def needsBackup(now) {
		def result=false
		def basePath=getBasePath()
		if (!basePath.exists()) {
			result = true
		} else {
			def files=basePath.listFiles()
			files.each { f ->
				if(f.getName().startsWith(name+"-")) {
					try {
						// try to parse the second half into a date
						def date=df.parse(f.getName().substring(f.getName().lastIndexOf("-")+1))
						
						result = (now-date >= interval.toInteger())
					} catch(Exception e) {
						log.info "${e}"
					}
				}
			}
			if (!files || files.size()==0) result=true;
		}
		return result
	}
	
	def getBasePath() {
		return new File(config.get("vcb.server.${server}.exportpath".toString())+"\\"+name)
	}
	
	def getNextBackupPath(now) {
		def bp=getBasePath()
		if (!bp.exists()) bp.mkdirs()
		return new File( bp.getAbsolutePath()+"\\"+ name + "-" + df.format(now) )
	}
	
	def saveLogFile(out,err) {
		def fout = new File(getBasePath().getAbsolutePath()+"\\vcb_out.txt")
		def ferr = new File(getBasePath().getAbsolutePath()+"\\vcb_err.txt")
		fout << out
		ferr << err
	}
	
	def purgeOldBackups() {
		// find all directories that contains a vm backup
		def backups=getBasePath().listFiles().findAll { f ->  f.getName().startsWith("${name}-") }
		// sort them by date desc
		backups = backups.sort { a,b -> 
				def dateA=df.parse(a.getName().substring(a.getName().lastIndexOf("-")+1));
				def dateB=df.parse(b.getName().substring(b.getName().lastIndexOf("-")+1));
				dateA.equals(dateB) ? 0 : dateA > dateB ? -1 : 1 
			}
		
		log.info "server ${name} backups: " + backups.toString()
		// remove all at index >= protection
		if (backups.size() > protection.toInteger()) {
            for(def i=protection.toInteger()-1;i<backups.size();i++) {
                log.info "server ${name} remove backup: "+backups[i]
                new AntBuilder().delete(dir: backups[i])
            }
        }
	}
}

def endTime=System.currentTimeMillis()
def duration=Math.round(((endTime-startTime)/60000))

log.info "--------------------------------------------"
log.info " Backed up ${backedup} vms in "+(duration)+"m"
log.info "--------------------------------------------"
