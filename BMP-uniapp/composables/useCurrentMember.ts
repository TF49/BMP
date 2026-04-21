import { ref } from 'vue'
import { getCurrentMember, type MemberInfo } from '@/api/member'

const currentMember = ref<MemberInfo | null>(null)
const currentMemberPromise = ref<Promise<MemberInfo> | null>(null)

export function useCurrentMember() {
  async function fetchCurrentMember(force = false) {
    if (!force && currentMember.value) {
      return currentMember.value
    }
    if (!force && currentMemberPromise.value) {
      return currentMemberPromise.value
    }

    const requestPromise = getCurrentMember()
      .then((member) => {
        currentMember.value = member
        return member
      })
      .finally(() => {
        currentMemberPromise.value = null
      })

    currentMemberPromise.value = requestPromise
    return requestPromise
  }

  function clearCurrentMember() {
    currentMember.value = null
    currentMemberPromise.value = null
  }

  return {
    currentMember,
    fetchCurrentMember,
    clearCurrentMember
  }
}
