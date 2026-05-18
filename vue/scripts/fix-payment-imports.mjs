import fs from 'fs'
import path from 'path'

const srcDir = new URL('../src/', import.meta.url)

function walk(dir, out = []) {
  for (const name of fs.readdirSync(dir)) {
    const p = path.join(dir, name)
    if (fs.statSync(p).isDirectory()) walk(p, out)
    else if (name.endsWith('.vue')) out.push(p)
  }
  return out
}

const loadFnByFile = {
  'Booking.vue': 'loadMyBookings',
  'CourseBooking.vue': 'loadMyCourses',
  'EquipmentRental.vue': 'loadMyRentals',
  'StringingService.vue': 'loadMyServices',
  'TournamentRegistration.vue': 'loadMyRegistrations'
}

for (const file of walk(srcDir.pathname)) {
  let s = fs.readFileSync(file, 'utf8')
  const orig = s
  s = s.replace(/getPaymentAutoCancelInfo, buildPaymentCountdownOptions, getPaymentAutoCancelInfo/g, 'buildPaymentCountdownOptions, getPaymentAutoCancelInfo')
  s = s.replace(/getPaymentAutoCancelInfo, getPaymentAutoCancelInfo/g, 'getPaymentAutoCancelInfo')
  s = s.replace(/\n  loadPaymentAutoCancelConfig\n/g, '\n')
  s = s.replace(/\s*loadPaymentAutoCancelConfig\(\)\s*\n/g, '\n')
  const base = path.basename(file)
  if (loadFnByFile[base]) {
    s = s.replace(/void loadList\(\)/g, `void ${loadFnByFile[base]}()`)
  }
  if (s !== orig) {
    fs.writeFileSync(file, s, 'utf8')
    console.log('fixed', path.relative(srcDir.pathname, file))
  }
}
